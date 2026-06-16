$portName = "COM3"
$baudRate = 9600

Write-Host "Iniciando lector FINAL en $portName..."
try {
    $port = New-Object System.IO.Ports.SerialPort $portName, $baudRate, "None", 8, "One"
    $port.DtrEnable = $true
    $port.RtsEnable = $true
    $port.Open()
    
    # Enviar comandos de activación
    $port.Write([byte[]](0x41, 0x49, 0x3F), 0, 3)
    
    Write-Host "=================================================="
    Write-Host ">>> LUZ ACTIVE DEBERÍA ESTAR ENCENDIDA AHORA <<<"
    Write-Host "TOMA TU TIEMPO (tienes 60 segundos)."
    Write-Host "Presiona los botones UNO POR UNO: Azul, luego Verde, luego Amarillo, luego Rojo."
    Write-Host "=================================================="
    
    $endTime = (Get-Date).AddSeconds(60)
    while ((Get-Date) -lt $endTime) {
        if ($port.BytesToRead -gt 0) {
            $data = $port.ReadExisting()
            $clean = $data -replace "`r", "" -replace "`n", ""
            if ($clean.Length -gt 0) {
                Write-Host ">>> NÚMERO RECIBIDO DE LA BOTONERA: $clean <<<"
            }
        }
        Start-Sleep -Milliseconds 100
    }
    
    $port.Close()
    Write-Host "Fin de la prueba."
} catch {
    Write-Host $_.Exception.Message
}
