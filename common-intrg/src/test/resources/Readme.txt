1) Extract the files under resources/properties and resources/xsd into resources Folder (For Ex: C:\ClaimStream\resources)
2) Extract StreamRegressionTest.bat into root Folder (For Ex: C:\ClaimStream)
3) Extract test files under /xml into Test Root Folder that is configured (TEST_ROOT_FOLDER=C\:\\ClaimStream\\TestXML)
4) Create Subfolders and place the respective xml files under each folder, For Ex: Customer Update xml should be placed under
C:\ClaimStream\TestXML\CUID for the configuration CUID=http://cms10ignas02:9081/iip/services/customerUpdate
5) Extract the library jar files from resources/lib into lib folder (C:\ClaimStream\lib)
6) Update the endpoint url to point to the server host and port.
7) Run the bat file. 