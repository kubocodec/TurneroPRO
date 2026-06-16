$portName = "COM3"
$baudRate = 9600

Write-Host "=========================================="
Write-Host "   SISTEMA TURNERO PRO - MODO RAW/DEBUG"
Write-Host "=========================================="

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
    Write-Host "[+] Puerto $portName abierto correctamente."
    Start-Sleep -Milliseconds 1000 # Wait for DTR reset
    
    Write-Host "[+] Enviando secuencia de DESPERTAR (AA, FF, 41, 61, 45)..."
    $sequence = @(0xAA, 0xFF, 0x41, 0x61, 0x45)
    foreach ($byte in $sequence) {
        $b = [byte[]]($byte)
        $port.Write($b, 0, 1)
        Start-Sleep -Milliseconds 100
    }
    
    Write-Host "[+] Luz ACTIVE encendida."
    Write-Host "[+] Escuchando... (Enviando 'R' cada 500ms)"
    Write-Host "------------------------------------------"

    while ($true) {
        # Enviar comando de Lectura (R)
        $r_bytes = [byte[]][char[]]"R"
        $port.Write($r_bytes, 0, $r_bytes.Length)
        
        Start-Sleep -Milliseconds 100
        
        # Leer todos los bytes que hayan llegado
        while ($port.BytesToRead -gt 0) {
            $byte = $port.ReadByte()
            $char = [char]$byte
            $hex = "{0:X2}" -f $byte
            
            Write-Host " [RAW] Dispositivo envio el Byte Hexadecimal: 0x$hex (Caracter: '$char')" -ForegroundColor Magenta
            
            if ($char -eq '1') { Write-Host "   -> BOTON AZUL" -ForegroundColor Cyan }
            elseif ($char -eq '2') { Write-Host "   -> BOTON VERDE" -ForegroundColor Green }
            elseif ($char -eq '3') { Write-Host "   -> BOTON AMARILLO" -ForegroundColor Yellow }
            elseif ($char -eq '4') { Write-Host "   -> BOTON ROJO" -ForegroundColor Red }
        }
        
        Start-Sleep -Milliseconds 400
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
