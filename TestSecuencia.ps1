$portName = "COM3"
$baudRate = 9600

Write-Host "Probando secuencia de Inicializacion..."

$port = New-Object System.IO.Ports.SerialPort
$port.PortName = $portName
$port.BaudRate = $baudRate
$port.DataBits = 8
$port.StopBits = [System.IO.Ports.StopBits]::One
$port.Parity = [System.IO.Ports.Parity]::None
$port.DtrEnable = $true
$port.RtsEnable = $true
$port.ReadTimeout = 500
$port.WriteTimeout = 500

try {
    $port.Open()
    Start-Sleep -Milliseconds 1000
    
    # Sequence based on the slow script
    $sequence = @(0xAA, 0xFF, 0x41, 0x61, 0x45)
    
    foreach ($byte in $sequence) {
        $msg = ">>> Enviando Hex: {0:X2}" -f $byte
        Write-Host $msg
        $b = [byte[]]($byte)
        $port.Write($b, 0, 1)
        Start-Sleep -Milliseconds 1000
    }
    
    Write-Host ">>> Secuencia enviada. Manteniendo el puerto abierto 15 segundos..."
    Start-Sleep -Seconds 15
}
catch {
    Write-Host "Error: $_" -ForegroundColor Red
}
finally {
    if ($port -ne $null -and $port.IsOpen) {
        $port.Close()
        Write-Host "Puerto cerrado."
    }
}
