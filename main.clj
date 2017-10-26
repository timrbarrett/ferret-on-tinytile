(configure-ferret! :command "~/Downloads/arduino-1.8.5/arduino \\
                               --board Intel:arc32:arduino_101 \\
                               --port /dev/ttyACM0 \\
                               --upload ./memory.cpp")

(native-header "MemoryFree.h")

(defn free[] "__result=obj<number>(freeMemory());")
(defn stack[] "__result=obj<number>(freeStack());")
(defn heap[] "__result=obj<number>(freeHeap());")

;;(native-declare "#define __clang__ 1")

;; how to test these great functions
(defn memory[] (list free stack heap))

(native-header "CurieBLE.h")
(native-declare "
  /*  BLE Definitions in C++
      Genuino is able to send and receive 20 bytes over BLE
      the other device is the device that connects

      I use nRF uart 2.0 by Nordic Semiconductor */

  const char* localName = \"Lisp\";
  static const int BLE_MAX_LENGTH = 20;
  char RXBuffer[BLE_MAX_LENGTH + 1];

  BLEService uartService = BLEService(\"6E400001-B5A3-F393-E0A9-E50E24DCCA9E\");
  BLEDescriptor uartNameDescriptor = BLEDescriptor(\"2901\", localName);

  BLECharacteristic rxCharacteristic = BLECharacteristic(\"6E400002-B5A3-F393-E0A9-E50E24DCCA9E\", BLEWrite, BLE_MAX_LENGTH);
  BLEDescriptor rxNameDescriptor = BLEDescriptor(\"2901\", \"RX - (Write)\");
  BLECharacteristic txCharacteristic = BLECharacteristic(\"6E400003-B5A3-F393-E0A9-E50E24DCCA9E\", BLEIndicate, BLE_MAX_LENGTH);
  BLEDescriptor txNameDescriptor = BLEDescriptor(\"2901\", \"TX - (Indicate)\");

  void rxCharacteristicWritten(BLECentral & central, BLECharacteristic & characteristic);
  boolean outputMemoryOnce = false;

  char outputBuffer[BLE_MAX_LENGTH];

  /* end BLE definitions */
")

(defn ble-start[] "

  /* Now activate the BLE device.  It will start continuously transmitting BLE
     advertising packets and will be visible to remote BLE central devices
     until it receives a new connection */
  BLE.begin();

  /* Set a local name for the BLE device
      This name will appear in advertising packets
      and can be used by remote devices to identify this BLE device
      The name can be changed but maybe be truncated based on space left in advertisement packet
  */
  BLE.setLocalName(localName);

  BLE.setAdvertisedService(uartService);

  uartService.addCharacteristic(txCharacteristic);           // add the transmit characteristic
  uartService.addCharacteristic(rxCharacteristic);           // the the receive characteristic

  BLE.addService(uartService);              // Add the uart service

  //BLE.setEventHandler(BLEConnected, blePeripheralConnectHandler);
  //BLE.setEventHandler(BLEDisconnected, blePeripheralDisconnectHandler);

  //rxCharacteristic.setEventHandler(BLEWritten, rxCharacteristicWritten);

  BLE.advertise();

")

(defn tx[text] "
  // TODO must process const char *text
  // will get passed an obj<string> thing as text

  temp_text=obj<string>text;
  // TODO handle when text is too long
  // TODO keep putting text in buffer until closing brace or 20 chars
  txCharacteristic.setValue((const unsigned char*)text, strlen(text)); //  temp

  // TODO test that this is non blocking
  BLE.poll();// also temp
")
((ble-start)
(println (free " " stack " " heap)))
