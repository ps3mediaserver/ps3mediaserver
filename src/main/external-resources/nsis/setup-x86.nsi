!macro checkSystemOS
!macroend

!macro installPrivateJRE
  File /r "${PROJECT_BASEDIR}\target\jre"
  File "${PROJECT_BASEDIR}\src\main\external-resources\PMS.bat"
  File /oname=win32\service\wrapper.conf "${PROJECT_BASEDIR}\src\main\external-resources\windows-service-wrapper\wrapper-x86.conf"
!macroend

!macro uninstallPrivateJRE
  RMDir /R /REBOOTOK "$INSTDIR\jre"
  Delete /REBOOTOK "$INSTDIR\PMS.bat"
!macroend

!include setup.nsi
