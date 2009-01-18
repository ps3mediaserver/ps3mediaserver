
!include "MUI.nsh"

!define REG_KEY_UNINSTALL "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\PS3 Media Server"
!define REG_KEY_SOFTWARE "SOFTWARE\PS3 Media Server"

RequestExecutionLevel admin

Name "PS3 Media Server"
OutFile "dist\pms-setup-win32-${VERSION}.exe"
InstallDir "$PROGRAMFILES\PS3 Media Server"

;Get install folder from registry for updates
InstallDirRegKey HKCU $REG_KEY_SOFTWARE ""

SetCompressor /SOLID lzma
SetCompressorDictSize 32
 
!define MUI_ABORTWARNING
!define MUI_FINISHPAGE_RUN "$INSTDIR\PMS.exe"
!define MUI_WELCOMEFINISHPAGE_BITMAP "${NSISDIR}\Contrib\Graphics\Wizard\win.bmp"

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_LANGUAGE "English"

ShowUninstDetails show

Section "Program Files"
  SetOutPath "$INSTDIR"
  SetOverwrite on
  File /r /x ".svn" "win32"
  File "PMS.exe"
  File "pms.jar"
  ;File "PMS.conf"
  File "WEB.conf"
  File "CHANGELOG"
  File "README"
  File "FAQ"
  File "LICENSE.txt"
 
  ;Store install folder
  WriteRegStr HKCU $REG_KEY_SOFTWARE "" $INSTDIR
 
 ;Create uninstaller
 WriteUninstaller "$INSTDIR\Uninst.exe"
 
  WriteRegStr HKEY_LOCAL_MACHINE $REG_KEY_UNINSTALL "DisplayName" "PS3 Media Server"
  WriteRegStr HKEY_LOCAL_MACHINE $REG_KEY_UNINSTALL "UninstallString" '"$INSTDIR\uninst.exe"'
  WriteUnInstaller "uninst.exe"
SectionEnd
 
Section "Start Menu Shortcuts"
  SetShellVarContext all
  CreateDirectory "$SMPROGRAMS\PS3 Media Server"
  CreateShortCut "$SMPROGRAMS\PS3 Media Server\PS3 Media Server.lnk" "$INSTDIR\PMS.exe" "" "$INSTDIR\PMS.exe" 0
  CreateShortCut "$SMPROGRAMS\PS3 Media Server\Uninstall.lnk" "$INSTDIR\uninst.exe" "" "$INSTDIR\uninst.exe" 0
SectionEnd
 
Section "Desktop shortcut"
  SetShellVarContext all
  CreateShortCut "$DESKTOP\PS3 Media Server.lnk" "$INSTDIR\pms.exe"
SectionEnd

Section "Uninstall"
  SetShellVarContext all

  Delete /REBOOTOK "$INSTDIR\Uninst.exe"
  RMDir /R /REBOOTOK "$INSTDIR\win32"
  Delete /REBOOTOK "$INSTDIR\PMS.exe"
  Delete /REBOOTOK "$INSTDIR\pms.jar"
  Delete /REBOOTOK "$INSTDIR\CHANGELOG"
  ;Delete /REBOOTOK "$INSTDIR\PMS.conf"
  Delete /REBOOTOK "$INSTDIR\WEB.conf"
  Delete /REBOOTOK "$INSTDIR\README"
  Delete /REBOOTOK "$INSTDIR\FAQ"
  Delete /REBOOTOK "$INSTDIR\LICENSE.txt"
  Delete /REBOOTOK "$INSTDIR\debug.log"
  RMDir /REBOOTOK "$INSTDIR"
  
 
  Delete /REBOOTOK "$DESKTOP\PS3 Media Server.lnk"
  RMDir /REBOOTOK "$SMPROGRAMS\PS3 Media Server"
  Delete /REBOOTOK "$SMPROGRAMS\PS3 Media Server\PS3 Media Server.lnk"
   Delete /REBOOTOK "$SMPROGRAMS\PS3 Media Server\Uninstall.lnk"
 
  DeleteRegKey HKEY_LOCAL_MACHINE $REG_KEY_UNINSTALL
  DeleteRegKey HKCU $REG_KEY_SOFTWARE
SectionEnd
