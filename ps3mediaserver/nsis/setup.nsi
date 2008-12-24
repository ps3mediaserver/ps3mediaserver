
!include "MUI.nsh"

!define REG_KEY_UNINSTALL "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\PS3 Media Server"
!define REG_KEY_SOFTWARE "SOFTWARE\PS3 Media Server"

RequestExecutionLevel admin

Name "PS3 Media Server"
OutFile "dist\pms-win32-${VERSION}.exe"
InstallDir "$PROGRAMFILES\PS3 Media Server"

;Get install folder from registry for updates
InstallDirRegKey HKCU $REG_KEY_SOFTWARE ""

SetCompressor /SOLID lzma
SetCompressorDictSize 32
 
!define MUI_ABORTWARNING
!define MUI_FINISHPAGE_RUN "$INSTDIR\PMS.exe"

;!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_LANGUAGE "English"

ShowUninstDetails show

Section "Program Files"
  SetOutPath "$INSTDIR"
  File /r "win32"
  File "pms.exe"
  File "pms.jar"
  File "pms.conf"
  File "WEB.conf"
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
  CreateShortCut "$SMPROGRAMS\PS3 Media Server.lnk" "$INSTDIR\PMS.exe" "" "$INSTDIR\PMS.exe" 0
SectionEnd
 
Section "Desktop shortcut"
  SetShellVarContext all
  CreateShortCut "$DESKTOP\PS3 Media Server.lnk" "$INSTDIR\pms.exe"
SectionEnd

Section "Uninstall"
  SetShellVarContext all

  Delete /REBOOTOK "$INSTDIR\Uninst.exe"
  RMDir /R /REBOOTOK "$INSTDIR"
 
  Delete /REBOOTOK "$DESKTOP\PS3 Media Server.lnk"
  Delete /REBOOTOK "$SMPROGRAMS\PS3 Media Server.lnk"
 
  DeleteRegKey HKEY_LOCAL_MACHINE $REG_KEY_UNINSTALL
  DeleteRegKey HKCU $REG_KEY_SOFTWARE
SectionEnd
