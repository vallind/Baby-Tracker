$ErrorActionPreference = 'Stop'
$root = 'D:\b\Baby-Tracker'
Set-Location $root
$src = 'app/src/main/java/com/babytracker/designsystem/components'

# git mv into domain dirs
git mv (Join-Path $src 'EmptyState.kt')            (Join-Path $src 'feedback/EmptyState.kt') | Out-Null
git mv (Join-Path $src 'EmptyStateDefaults.kt')    (Join-Path $src 'feedback/EmptyStateDefaults.kt') | Out-Null
git mv (Join-Path $src 'SegmentedControl.kt')      (Join-Path $src 'selection/SegmentedControl.kt') | Out-Null
git mv (Join-Path $src 'SegmentedControlDefaults.kt') (Join-Path $src 'selection/SegmentedControlDefaults.kt') | Out-Null
git mv (Join-Path $src 'BadgeIcon.kt')             (Join-Path $src 'badge/BadgeIcon.kt') | Out-Null
Write-Output "moved 5 files"

# fix package lines of moved files
$pkgFixes = @(
  @("$src/feedback/EmptyState.kt", 'package com.babytracker.designsystem.components', 'package com.babytracker.designsystem.components.feedback'),
  @("$src/feedback/EmptyStateDefaults.kt", 'package com.babytracker.designsystem.components', 'package com.babytracker.designsystem.components.feedback'),
  @("$src/selection/SegmentedControl.kt", 'package com.babytracker.designsystem.components', 'package com.babytracker.designsystem.components.selection'),
  @("$src/selection/SegmentedControlDefaults.kt", 'package com.babytracker.designsystem.components', 'package com.babytracker.designsystem.components.selection'),
  @("$src/badge/BadgeIcon.kt", 'package com.babytracker.designsystem.components', 'package com.babytracker.designsystem.components.badge')
)
foreach ($f in $pkgFixes) {
  $text = [System.IO.File]::ReadAllText($f[0])
  if ($text.Contains($f[1])) {
    $text = $text.Replace($f[1], $f[2])
    [System.IO.File]::WriteAllText($f[0], $text, (New-Object System.Text.UTF8Encoding($false)))
    Write-Output "fixed package: $($f[0])"
  } else { Write-Error "package line not found in $($f[0])" }
}

# update imports everywhere (main + test)
$importFixes = @(
  @('import com.babytracker.designsystem.components.EmptyState',         'import com.babytracker.designsystem.components.feedback.EmptyState'),
  @('import com.babytracker.designsystem.components.EmptyStateDefaults', 'import com.babytracker.designsystem.components.feedback.EmptyStateDefaults'),
  @('import com.babytracker.designsystem.components.SegmentedControl',      'import com.babytracker.designsystem.components.selection.SegmentedControl'),
  @('import com.babytracker.designsystem.components.SegmentedControlDefaults','import com.babytracker.designsystem.components.selection.SegmentedControlDefaults'),
  @('import com.babytracker.designsystem.components.BadgeIcon',          'import com.babytracker.designsystem.components.badge.BadgeIcon')
)
$files = Get-ChildItem app/src/main/java, app/src/test/java -Recurse -Filter *.kt
$changed = 0
foreach ($f in $files) {
  $text = [System.IO.File]::ReadAllText($f.FullName)
  $orig = $text
  foreach ($fix in $importFixes) { $text = $text.Replace($fix[0], $fix[1]) }
  if ($text -ne $orig) {
    [System.IO.File]::WriteAllText($f.FullName, $text, (New-Object System.Text.UTF8Encoding($false)))
    $changed++
  }
}
Write-Output "updated importing files: $changed"
Write-Output "SUB-DIR DONE"