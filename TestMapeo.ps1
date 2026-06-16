$portName = "COM3"
$baudRate = 9600

Write-Host "Iniciando lector de mapeo en $portName a $baudRate baudios..."
try {
    $port = New-Object System.IO.Ports.SerialPort $portName, $baudRate, "None", 8, "One"
    $port.DtrEnable = $true
    $port.RtsEnable = $true
    $port.Open()
    
    # Enviar comandos de activacion descubiertos (?, I, A)
    $initCmds = [byte[]](0x3F, 0x49, 0x41)
    $port.Write($initCmds, 0, $initCmds.Length)
    
    Write-Host "PUERTO ABIERTO Y ACTIVADO."
    Write-Host "¡PRESIONA LOS BOTONES AHORA EN ORDEN (Azul, Verde, Amarillo, Rojo)!"
    
    $endTime = (Get-Date).AddSeconds(30)
    while ((Get-Date) -lt $endTime) {
        $bytesToRead = $port.BytesToRead
        if ($bytesToRead -gt 0) {
            $buffer = New-Object byte[] $bytesToRead
            $readCount = $port.Read($buffer, 0, $bytesToRead)
            $text = [System.Text.Encoding]::ASCII.GetString($buffer, 0, $readCount)
            $cleanText = $text -replace "`r", "" -replace "`n", ""
            if ($cleanText.Length -gt 0) {
                Write-Host ">>> Boton presionado: $cleanText <<<"
            }
        }
        Start-Sleep -Milliseconds 100
    }
    
    $port.Close()
    Write-Host "Fin de la prueba de mapeo."
} catch {
    Write-Host $_.Exception.Message
}
