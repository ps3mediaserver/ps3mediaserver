!macro checkSystemOS
!macroend

!macro privateJRE
  File /r "${PROJECT_BASEDIR}\target\jre"
!macroend

!include setup.nsi
