type ProviderProtocol = "openai_responses" | "openai_compatible_chat";

type RuntimeProvider = {
  id: string;
  protocol: ProviderProtocol;
  baseUrl: string;
  credentialVersion: number;
  credentialEnv: string;
};

type ModelTarget = {
  providerId: string;
  model: string;
  priority?: number;
};

type ModelOption = {
  id: string;
  name: string;
  targets: ModelTarget[];
  maxOutputTokens?: number;
  capabilities?: ModelCapabilities;
};

type ModelCapabilities = {
  streaming?: boolean;
  thinking?: boolean;
  reasoningEfforts?: Array<"low" | "medium" | "high" | "max">;
  temperature?: boolean;
};

type RuntimeConfig = {
  configVersion: number;
  defaultOption: string;
  ttlSeconds?: number;
  providers: RuntimeProvider[];
  options: ModelOption[];
};

const JSON_HEADERS = {
  "Content-Type": "application/json; charset=utf-8",
  "Cache-Control": "no-store",
};

Deno.serve(async (request: Request) => {
  if (request.method !== "POST") {
    return jsonResponse({ error: "method_not_allowed" }, 405);
  }

  try {
    const authorization = request.headers.get("Authorization") ?? "";
    if (!authorization.startsWith("Bearer ")) {
      return jsonResponse({ error: "unauthorized" }, 401);
    }

    const body = await request.json() as Record<string, unknown>;
    const familyId = requiredString(body.familyId, "familyId", 128);
    const publicKeyBase64 = requiredString(body.devicePublicKey, "devicePublicKey", 8_192);

    const supabaseUrl = requiredEnv("SUPABASE_URL");
    const anonKey = requiredEnv("SUPABASE_ANON_KEY");
    const userId = await getVerifiedUserId(supabaseUrl, anonKey, authorization);
    const isMember = await verifyFamilyMembership(
      supabaseUrl,
      anonKey,
      authorization,
      userId,
      familyId,
    );
    if (!isMember) {
      return jsonResponse({ error: "family_forbidden" }, 403);
    }

    const config = loadRuntimeConfig();
    validateRuntimeConfig(config);
    const publicKey = await importDevicePublicKey(publicKeyBase64);
    const credentials = await Promise.all(config.providers.map(async (provider) => {
      const credential = requiredEnv(provider.credentialEnv);
      const bytes = new TextEncoder().encode(credential);
      // RSA-2048 OAEP-SHA256 最多承载 190 字节，供应商密钥超过时应改用混合加密。
      if (bytes.byteLength > 190) {
        throw new Error(`credential_too_long:${provider.id}`);
      }
      const encrypted = await crypto.subtle.encrypt({ name: "RSA-OAEP" }, publicKey, bytes);
      return {
        providerId: provider.id,
        credentialVersion: provider.credentialVersion,
        envelope: bytesToBase64(new Uint8Array(encrypted)),
      };
    }));

    const ttlSeconds = Math.min(Math.max(config.ttlSeconds ?? 604_800, 3_600), 2_592_000);
    const publicConfig = {
      configVersion: config.configVersion,
      defaultOption: config.defaultOption,
      providers: config.providers.map((provider) => ({
        id: provider.id,
        protocol: provider.protocol,
        baseUrl: provider.baseUrl,
        credentialVersion: provider.credentialVersion,
      })),
      options: config.options.map((option) => ({
        ...option,
        maxOutputTokens: option.maxOutputTokens ?? 2_400,
        capabilities: {
          streaming: option.capabilities?.streaming ?? false,
          thinking: option.capabilities?.thinking ?? false,
          reasoningEfforts: option.capabilities?.reasoningEfforts ?? [],
          temperature: option.capabilities?.temperature ?? false,
        },
        targets: option.targets.map((target) => ({
          ...target,
          priority: target.priority ?? 0,
        })),
      })),
    };

    return jsonResponse({
      configVersion: config.configVersion,
      expiresAt: Date.now() + ttlSeconds * 1_000,
      config: publicConfig,
      credentials,
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : "unknown_error";
    if (message === "invalid_session") return jsonResponse({ error: message }, 401);
    if (message.startsWith("invalid_request:")) return jsonResponse({ error: message }, 400);
    // 不把环境变量值、供应商响应或堆栈返回给客户端。
    console.error("ai-bootstrap failed", message);
    return jsonResponse({ error: "bootstrap_unavailable" }, 503);
  }
});

function loadRuntimeConfig(): RuntimeConfig {
  const raw = requiredEnv("AI_RUNTIME_CONFIG");
  try {
    return JSON.parse(raw) as RuntimeConfig;
  } catch {
    throw new Error("invalid_runtime_config_json");
  }
}

function validateRuntimeConfig(config: RuntimeConfig): void {
  if (!Number.isInteger(config.configVersion) || config.configVersion <= 0) {
    throw new Error("invalid_config_version");
  }
  if (!Array.isArray(config.providers) || config.providers.length === 0) {
    throw new Error("providers_missing");
  }
  if (!Array.isArray(config.options) || config.options.length === 0) {
    throw new Error("options_missing");
  }

  const providerIds = new Set<string>();
  for (const provider of config.providers) {
    if (!/^[a-z0-9_-]{1,48}$/.test(provider.id) || providerIds.has(provider.id)) {
      throw new Error("invalid_or_duplicate_provider_id");
    }
    providerIds.add(provider.id);
    if (!["openai_responses", "openai_compatible_chat"].includes(provider.protocol)) {
      throw new Error(`unsupported_protocol:${provider.id}`);
    }
    const url = new URL(provider.baseUrl);
    if (url.protocol !== "https:") throw new Error(`provider_requires_https:${provider.id}`);
    if (!Number.isInteger(provider.credentialVersion) || provider.credentialVersion <= 0) {
      throw new Error(`invalid_credential_version:${provider.id}`);
    }
    if (!/^AI_PROVIDER_[A-Z0-9_]+_KEY$/.test(provider.credentialEnv)) {
      throw new Error(`invalid_credential_env:${provider.id}`);
    }
  }

  const optionIds = new Set<string>();
  for (const option of config.options) {
    if (!option.id || optionIds.has(option.id) || !option.name) {
      throw new Error("invalid_or_duplicate_option_id");
    }
    optionIds.add(option.id);
    if (!Array.isArray(option.targets) || option.targets.length === 0) {
      throw new Error(`targets_missing:${option.id}`);
    }
    const efforts = option.capabilities?.reasoningEfforts ?? [];
    if (!Array.isArray(efforts) ||
      efforts.some((effort) => !["low", "medium", "high", "max"].includes(effort))) {
      throw new Error(`invalid_reasoning_efforts:${option.id}`);
    }
    for (const target of option.targets) {
      if (!providerIds.has(target.providerId) || !target.model) {
        throw new Error(`invalid_target:${option.id}`);
      }
    }
  }
  if (!optionIds.has(config.defaultOption)) throw new Error("default_option_missing");
}

async function getVerifiedUserId(
  supabaseUrl: string,
  anonKey: string,
  authorization: string,
): Promise<string> {
  const response = await fetch(`${supabaseUrl}/auth/v1/user`, {
    headers: { apikey: anonKey, Authorization: authorization },
  });
  if (!response.ok) throw new Error("invalid_session");
  const user = await response.json() as { id?: string };
  if (!user.id) throw new Error("invalid_session");
  return user.id;
}

async function verifyFamilyMembership(
  supabaseUrl: string,
  anonKey: string,
  authorization: string,
  userId: string,
  familyId: string,
): Promise<boolean> {
  const url = new URL(`${supabaseUrl}/rest/v1/family_members`);
  url.searchParams.set("select", "family_id");
  url.searchParams.set("family_id", `eq.${familyId}`);
  url.searchParams.set("user_id", `eq.${userId}`);
  url.searchParams.set("limit", "1");
  const response = await fetch(url, {
    headers: {
      apikey: anonKey,
      Authorization: authorization,
      Accept: "application/json",
    },
  });
  if (!response.ok) return false;
  const rows = await response.json() as unknown[];
  return rows.length === 1;
}

async function importDevicePublicKey(base64: string): Promise<CryptoKey> {
  try {
    return await crypto.subtle.importKey(
      "spki",
      base64ToBytes(base64),
      { name: "RSA-OAEP", hash: "SHA-256" },
      false,
      ["encrypt"],
    );
  } catch {
    throw new Error("invalid_request:devicePublicKey");
  }
}

function requiredString(value: unknown, name: string, maxLength: number): string {
  if (typeof value !== "string" || value.length === 0 || value.length > maxLength) {
    throw new Error(`invalid_request:${name}`);
  }
  return value;
}

function requiredEnv(name: string): string {
  const value = Deno.env.get(name);
  if (!value) throw new Error(`missing_env:${name}`);
  return value;
}

function base64ToBytes(value: string): Uint8Array {
  const binary = atob(value.replace(/\s/g, ""));
  return Uint8Array.from(binary, (char) => char.charCodeAt(0));
}

function bytesToBase64(value: Uint8Array): string {
  let binary = "";
  value.forEach((byte) => binary += String.fromCharCode(byte));
  return btoa(binary);
}

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), { status, headers: JSON_HEADERS });
}
