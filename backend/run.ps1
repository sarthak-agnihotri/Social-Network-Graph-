# Automate backend compilation and server start
Write-Host "--- Starting Backend Build ---" -ForegroundColor Cyan

# 1. Create bin folder
if (!(Test-Path bin)) { New-Item -ItemType Directory -Path bin }

# 2. Compile
Write-Host "Compiling Java files..." -ForegroundColor Yellow
$files = Get-ChildItem -Path src -Filter *.java -Recurse | Select-Object -ExpandProperty FullName
[System.IO.File]::WriteAllLines("$pwd\sources.txt", $files)
javac -d bin "@sources.txt"

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful!" -ForegroundColor Green
    Write-Host "Starting server on port 8000..." -ForegroundColor Cyan
    java -cp bin com.socialgraph.server.SimpleHttpServer
} else {
    Write-Host "Compilation failed. Please check your code." -ForegroundColor Red
}
