!include "x64.nsh"

!macro checkSystemOS
  ${IfNot} ${RunningX64}
    MessageBox MB_ICONSTOP "Can't install on non 64-bit OS. Please download and install PMS-setup-full.exe."
    Abort
  ${EndIf}
!macroend

!macro privateJRE
  File /r "${PROJECT_BASEDIR}\target\jre64"
!macroend

!include setup.nsi
