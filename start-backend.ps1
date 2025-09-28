#!/usr/bin/env pwsh

Write-Host "Starting AndroidInsta Spring Boot Backend..." -ForegroundColor Green

# Navigate to backend directory  
Set-Location "D:\AndroidInsta\spring_boot_backend"

Write-Host "Current directory: $(Get-Location)" -ForegroundColor Yellow

# Check if pom.xml exists
if (-not (Test-Path "pom.xml")) {
    Write-Host "pom.xml not found in current directory!" -ForegroundColor Red
    exit 1
}

Write-Host "Found pom.xml, starting Spring Boot..." -ForegroundColor Green

# Start Spring Boot application
try {
    mvn spring-boot:run
} catch {
    Write-Host "Error starting Spring Boot: $_" -ForegroundColor Red
    exit 1
}