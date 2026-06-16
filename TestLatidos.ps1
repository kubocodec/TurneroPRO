$portName = "COM3"
$baudRate = 9600

Write-Host "Iniciando Lector de Turnero Pro..."
Write-Host "=================================="

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
    Write-Host "Puerto $portName abierto correctamente."
    
    # Send Initialization / Enable (E)
    Write-Host ">>> Enviando 'E' (Enable / Activar Luz)..."
    $bytes = [byte[]][char[]]"E"
    $port.Write($bytes, 0, $bytes.Length)
    
    Start-Sleep -Milliseconds 500

    Write-Host ">>> Iniciando bucle de lectura con 'R' (Heartbeat/Read)..."
    while ($true) {
        # Keep alive and ask for buttons
        $r_bytes = [byte[]][char[]]"R"
        $port.Write($r_bytes, 0, $r_bytes.Length)
        
        Start-Sleep -Milliseconds 200
        
        while ($port.BytesToRead -gt 0) {
            $byte = $port.ReadByte()
            $char = [char]$byte
            $hex = "{0:X2}" -f $byte
            Write-Host "[!] BOTON PRESIONADO: $char (Hex: $hex)" -ForegroundColor Green
            
            if ($char -eq '1') { Write-Host "   -> BOTON AZUL (Excelente)" -ForegroundColor Cyan }
            if ($char -eq '2') { Write-Host "   -> BOTON VERDE (Bueno)" -ForegroundColor Green }
            if ($char -eq '3') { Write-Host "   -> BOTON AMARILLO (Regular)" -ForegroundColor Yellow }
            if ($char -eq '4') { Write-Host "   -> BOTON ROJO (Malo)" -ForegroundColor Red }
        }
        
        Start-Sleep -Milliseconds 800
    }
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
