!macro checkSystemOS
!macroend

!macro installPrivateJRE
  File "${PROJECT_BASEDIR}\src\main\external-resources\PMS.bat"
!macroend

!macro uninstallPrivateJRE
  Delete /REBOOTOK "$INSTDIR\PMS.bat"
!macroend

!include setup.nsi
