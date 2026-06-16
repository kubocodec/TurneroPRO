$portName = "COM3"
$baudRate = 9600

Write-Host "Iniciando Prueba Definitiva en $portName..."
try {
    $port = New-Object System.IO.Ports.SerialPort $portName, $baudRate, "None", 8, "One"
    $port.DtrEnable = $true
    $port.RtsEnable = $true
    $port.Open()
    
    Write-Host "Ejecutando secuencia de inicializacion magica..."
    $port.Write([byte[]](0x49), 0, 1) # I
    Start-Sleep -Seconds 8
    
    $port.Write([byte[]](0x3F), 0, 1) # ?
    Start-Sleep -Seconds 8
    
    $port.Write([byte[]](0x53), 0, 1) # S
    
    Write-Host "=================================================="
    Write-Host ">>> LUZ ACTIVE DEBE ESTAR ENCENDIDA FIJA AHORA <<<"
    Write-Host "Tienes 60 segundos. Presiona: Azul, Verde, Amarillo, Rojo."
    Write-Host "=================================================="
    
    $endTime = (Get-Date).AddSeconds(60)
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
