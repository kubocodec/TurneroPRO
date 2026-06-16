$portName = "COM3"
$baudRate = 9600

try {
    $port = New-Object System.IO.Ports.SerialPort $portName, $baudRate, "None", 8, "One"
    $port.DtrEnable = $true
    $port.RtsEnable = $true
    $port.Open()
    
    $pollCmd = [System.Text.Encoding]::ASCII.GetBytes("?")
    Write-Host "PUERTO ABIERTO. Haciendo polling continuo con '?'..."
    Write-Host "¡PRESIONA LOS BOTONES AHORA!"
    
    $endTime = (Get-Date).AddSeconds(25)
    while ((Get-Date) -lt $endTime) {
        $port.Write($pollCmd, 0, $pollCmd.Length)
        Start-Sleep -Milliseconds 50
        
        $bytesToRead = $port.BytesToRead
        if ($bytesToRead -gt 0) {
            $buffer = New-Object byte[] $bytesToRead
            $readCount = $port.Read($buffer, 0, $bytesToRead)
            $text = [System.Text.Encoding]::ASCII.GetString($buffer, 0, $readCount)
            $cleanText = $text -replace "`r", "" -replace "`n", ""
            if ($cleanText.Length -gt 0 -and $cleanText -ne "?") {
                Write-Host ">>> BOTON PRESIONADO: $cleanText <<<"
            }
        }
        Start-Sleep -Milliseconds 300
    }
    
    $port.Close()
    Write-Host "Fin de la prueba."
} catch {
    Write-Host $_.Exception.Message
}
