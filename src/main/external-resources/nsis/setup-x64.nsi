!include "x64.nsh"

!macro checkSystemOS
  ${IfNot} ${RunningX64}
    MessageBox MB_ICONSTOP "Can't install on non 64-bit OS. Please download and install PMS-setup-full.exe."
    Abort
  ${EndIf}
!macroend

!macro installPrivateJRE
  File /r "${PROJECT_BASEDIR}\target\jre64"
  File "${PROJECT_BASEDIR}\src\main\external-resources\PMS-x64.bat"
  File /oname=win32\service\wrapper.conf "${PROJECT_BASEDIR}\src\main\external-resources\windows-service-wrapper\wrapper-x64.conf"
!macroend

!macro uninstallPrivateJRE
  RMDir /R /REBOOTOK "$INSTDIR\jre64"
  Delete /REBOOTOK "$INSTDIR\PMS-x64.bat"
!macroend

!include setup.nsi
