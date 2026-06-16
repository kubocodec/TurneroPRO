$portName = "COM3"
$baudRate = 9600

Write-Host "Iniciando lector SOLO E..."
try {
    $port = New-Object System.IO.Ports.SerialPort $portName, $baudRate, "None", 8, "One"
    $port.DtrEnable = $true
    $port.RtsEnable = $true
    $port.Open()
    
    # Enviar solo E (0x45)
    $port.Write([byte[]](0x45), 0, 1)
    Write-Host "=================================================="
    Write-Host ">>> ENVIADA 'E' - DEBERIA ENCENDER <<<"
    Write-Host "=================================================="
    
    $endTime = (Get-Date).AddSeconds(120)
    while ((Get-Date) -lt $endTime) {
        if ($port.BytesToRead -gt 0) {
            $data = $port.ReadExisting()
            $clean = $data -replace "`r", "" -replace "`n", ""
            if ($clean.Length -gt 0) {
                Write-Host ">>> RECIBIDO: $clean <<<"
            }
        }
        Start-Sleep -Milliseconds 100
    }
    
    $port.Close()
} catch {
    Write-Host $_.Exception.Message
}
