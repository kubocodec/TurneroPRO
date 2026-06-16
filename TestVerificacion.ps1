$portName = "COM3"
$baudRate = 9600

Write-Host "Iniciando lector DEFINITIVO de mapeo en $portName..."
try {
    $port = New-Object System.IO.Ports.SerialPort $portName, $baudRate, "None", 8, "One"
    $port.DtrEnable = $true
    $port.RtsEnable = $true
    $port.Open()
    
    # Enviar comando de activación S (Start)
    $port.Write([byte[]](0x53), 0, 1)
    
    Write-Host "=================================================="
    Write-Host ">>> LUZ ACTIVE ENCENDIDA <<<"
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
        
        # Opcionalmente, podemos enviar R para leer si es que requiere polling
        # $port.Write([byte[]](0x52), 0, 1)
        Start-Sleep -Milliseconds 100
    }
    
    $port.Close()
    Write-Host "Fin de la prueba."
} catch {
    Write-Host $_.Exception.Message
}
