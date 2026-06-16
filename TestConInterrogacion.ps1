$portName = "COM3"
$baudRate = 9600

try {
    $port = New-Object System.IO.Ports.SerialPort $portName, $baudRate, "None", 8, "One"
    $port.DtrEnable = $true
    $port.RtsEnable = $true
    $port.Open()
    
    # Enviar ? (0x3F)
    $port.Write([byte[]](0x3F), 0, 1)
    
    Write-Host "=================================================="
    Write-Host ">>> SE ENVIO '?'. LUZ ACTIVE DEBERIA ESTAR ENCENDIDA <<<"
    Write-Host "Tienes 2 MINUTOS. Presiona: Azul, Verde, Amarillo, Rojo."
    Write-Host "=================================================="
    
    $endTime = (Get-Date).AddSeconds(120)
    while ((Get-Date) -lt $endTime) {
        if ($port.BytesToRead -gt 0) {
            $data = $port.ReadExisting()
            $clean = $data -replace "`r", "" -replace "`n", ""
            if ($clean.Length -gt 0) {
                Write-Host ">>> BOTON PRESIONADO: $clean <<<"
            }
        }
        Start-Sleep -Milliseconds 100
    }
    
    $port.Close()
    Write-Host "Fin de la prueba."
} catch {
    Write-Host $_.Exception.Message
}
