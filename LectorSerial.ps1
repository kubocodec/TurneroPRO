$portName = "COM3"
$baudRate = 9600

Write-Host "Iniciando lector de puerto serial en $portName a $baudRate baudios con DTR/RTS activos..."
try {
    $port = New-Object System.IO.Ports.SerialPort $portName, $baudRate, "None", 8, "One"
    $port.DtrEnable = $true
    $port.RtsEnable = $true
    $port.Open()
    Write-Host "¡Puerto $portName abierto exitosamente!"
    Write-Host "POR FAVOR, PRESIONA LOS BOTONES EN TU BOTONERA AHORA (Malo, Regular, Bueno, Excelente)..."
    
    $endTime = (Get-Date).AddSeconds(45)
    while ((Get-Date) -lt $endTime) {
        $bytesToRead = $port.BytesToRead
        if ($bytesToRead -gt 0) {
            $buffer = New-Object byte[] $bytesToRead
            $readCount = $port.Read($buffer, 0, $bytesToRead)
            $hex = [System.BitConverter]::ToString($buffer, 0, $readCount)
            $text = [System.Text.Encoding]::ASCII.GetString($buffer, 0, $readCount)
            
            # Limpiar caracteres invisibles en el texto para impresión
            $cleanText = $text -replace "`r", "\r" -replace "`n", "\n"
            Write-Host "-> DATOS RECIBIDOS: Hex=$hex | Texto='$cleanText'"
        }
        Start-Sleep -Milliseconds 100
    }
    
    $port.Close()
    Write-Host "Lector finalizado tras 45 segundos."
} catch {
    Write-Host "Error al abrir el puerto $portName."
    Write-Host $_.Exception.Message
}
