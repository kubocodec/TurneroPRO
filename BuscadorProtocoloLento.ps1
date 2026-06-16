$portName = "COM3"
$baudRate = 9600

$commands = @(
    [byte[]](0x00),
    [byte[]](0x01),
    [byte[]](0x02),
    [byte[]](0x04),
    [byte[]](0x05),
    [byte[]](0x0A),
    [byte[]](0x0D),
    [byte[]](0x1B),
    [byte[]](0xAA),
    [byte[]](0xFF),
    [System.Text.Encoding]::ASCII.GetBytes("A"),
    [System.Text.Encoding]::ASCII.GetBytes("a"),
    [System.Text.Encoding]::ASCII.GetBytes("E"),
    [System.Text.Encoding]::ASCII.GetBytes("I"),
    [System.Text.Encoding]::ASCII.GetBytes("?"),
    [System.Text.Encoding]::ASCII.GetBytes("S"),
    [System.Text.Encoding]::ASCII.GetBytes("R"),
    [System.Text.Encoding]::ASCII.GetBytes("1"),
    [System.Text.Encoding]::ASCII.GetBytes("INIT")
)

try {
    $port = New-Object System.IO.Ports.SerialPort $portName, $baudRate, "None", 8, "One"
    $port.DtrEnable = $true
    $port.RtsEnable = $true
    $port.Open()
    Write-Host "Puerto $portName abierto. Iniciando escaneo LENTO (8 segundos por comando)..."
    
    foreach ($cmd in $commands) {
        $hex = [System.BitConverter]::ToString($cmd)
        Write-Host ""
        Write-Host "----------------------------------------"
        Write-Host ">>> ENVIANDO COMANDO HEX: $hex <<<"
        
        $port.Write($cmd, 0, $cmd.Length)
        
        # Esperar 8 segundos por comando
        for ($i=0; $i -lt 80; $i++) {
            if ($port.BytesToRead -gt 0) {
                $bytesToRead = $port.BytesToRead
                $buffer = New-Object byte[] $bytesToRead
                $readCount = $port.Read($buffer, 0, $bytesToRead)
                $respHex = [System.BitConverter]::ToString($buffer, 0, $readCount)
                Write-Host "    [!] RESPUESTA RECIBIDA: $respHex"
            }
            Start-Sleep -Milliseconds 100
        }
    }
    
    $port.Close()
    Write-Host "----------------------------------------"
    Write-Host "Fin de la prueba lenta."
} catch {
    Write-Host $_.Exception.Message
}
