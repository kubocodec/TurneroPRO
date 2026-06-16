$portName = "COM3"
$baudRate = 9600

Write-Host "Iniciando lector con comando S en $portName..."
try {
    $port = New-Object System.IO.Ports.SerialPort $portName, $baudRate, "None", 8, "One"
    $port.DtrEnable = $true
    $port.RtsEnable = $true
    $port.Open()
    
    # Enviar la letra S (HEX 53) que parece ser el activador
    $port.Write([byte[]](0x53), 0, 1)
    
    Write-Host "=================================================="
    Write-Host ">>> SE ENVIO 'S'. LA LUZ ACTIVE DEBERIA ESTAR ENCENDIDA AHORA MISMO <<<"
    Write-Host "Presiona los botones UNO POR UNO: Azul, Verde, Amarillo, Rojo."
    Write-Host "=================================================="
    
    $endTime = (Get-Date).AddSeconds(60)
    while ((Get-Date) -lt $endTime) {
        if ($port.BytesToRead -gt 0) {
            $data = $port.ReadExisting()
            $clean = $data -replace "`r", "" -replace "`n", ""
            if ($clean.Length -gt 0) {
                Write-Host ">>> NUMERO RECIBIDO DE LA BOTONERA: $clean <<<"
            }
        }
        Start-Sleep -Milliseconds 100
    }
    
    $port.Close()
    Write-Host "Fin de la prueba S."
} catch {
    Write-Host $_.Exception.Message
}
