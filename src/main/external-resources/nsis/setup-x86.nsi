!macro checkSystemOS
!macroend

!macro installPrivateJRE
  File /r "${PROJECT_BASEDIR}\target\jre"
  File "${PROJECT_BASEDIR}\src\main\external-resources\PMS.bat"
!macroend

!macro uninstallPrivateJRE
  RMDir /R /REBOOTOK "$INSTDIR\jre"
  Delete /REBOOTOK "$INSTDIR\PMS.bat"
!macroend

!include setup.nsi
