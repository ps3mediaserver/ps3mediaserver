
!include "MUI.nsh"
!include "FileFunc.nsh"

!define REG_KEY_UNINSTALL "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\PS3 Media Server"
!define REG_KEY_SOFTWARE "SOFTWARE\PS3 Media Server"

RequestExecutionLevel admin

Name "PS3 Media Server"
OutFile "dist\pms-setup-windows-${VERSION}.exe"
InstallDir "$PROGRAMFILES\PS3 Media Server"

;Get install folder from registry for updates
InstallDirRegKey HKCU "${REG_KEY_SOFTWARE}" ""

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
  File /r /x ".svn" /x "*.jar" /x "*.conf" /x "*.zip" /x "*.dll" /x "third-party" "plugins"
  File /r /x ".svn" "documentation"
  File /r /x ".svn" "renderers"
  File /r /x ".svn" "win32"
  File "PMS.exe"
  File "PMS.bat"
  File "pms.jar"
  File "MediaInfo.dll"
  File "MediaInfo64.dll"
  File "CHANGELOG"
  File "README"
  File "LICENSE.txt"
  File "logback.xml"
  File "icon.ico"
  
  ;Store install folder
  WriteRegStr HKCU "${REG_KEY_SOFTWARE}" "" $INSTDIR
 
 ;Create uninstaller
 WriteUninstaller "$INSTDIR\Uninst.exe"

  WriteRegStr HKEY_LOCAL_MACHINE "${REG_KEY_UNINSTALL}" "DisplayName" "PS3 Media Server"
  WriteRegStr HKEY_LOCAL_MACHINE "${REG_KEY_UNINSTALL}" "DisplayIcon" "$INSTDIR\icon.ico"
  WriteRegStr HKEY_LOCAL_MACHINE "${REG_KEY_UNINSTALL}" "DisplayVersion" "1.30.2"
  WriteRegStr HKEY_LOCAL_MACHINE "${REG_KEY_UNINSTALL}" "Publisher" "PS3 Media Server"
  WriteRegStr HKEY_LOCAL_MACHINE "${REG_KEY_UNINSTALL}" "URLInfoAbout" "http://www.ps3mediaserver.org"
  WriteRegStr HKEY_LOCAL_MACHINE "${REG_KEY_UNINSTALL}" "UninstallString" '"$INSTDIR\uninst.exe"'

  ${GetSize} "$INSTDIR" "/S=0K" $0 $1 $2
  IntFmt $0 "0x%08X" $0
  WriteRegDWORD HKLM "${REG_KEY_UNINSTALL}" "EstimatedSize" "$0"

  WriteUnInstaller "uninst.exe"

  ReadENVStr $R0 ALLUSERSPROFILE
  SetOutPath "$R0\PMS"
  SetOverwrite off
  File "WEB.conf"
  AccessControl::GrantOnFile "$R0\PMS" "(S-1-1-0)" "FullAccess"
SectionEnd
 
Section "Start Menu Shortcuts"
  SetShellVarContext all
  CreateDirectory "$SMPROGRAMS\PS3 Media Server"
  CreateShortCut "$SMPROGRAMS\PS3 Media Server\PS3 Media Server.lnk" "$INSTDIR\PMS.exe" "" "$INSTDIR\PMS.exe" 0
  CreateShortCut "$SMPROGRAMS\PS3 Media Server\PS3 Media Server (Select Profile).lnk" "$INSTDIR\PMS.exe" "profiles" "$INSTDIR\PMS.exe" 0
  CreateShortCut "$SMPROGRAMS\PS3 Media Server\Uninstall.lnk" "$INSTDIR\uninst.exe" "" "$INSTDIR\uninst.exe" 0
SectionEnd
 
Section "Desktop shortcut"
  SetShellVarContext all
  CreateShortCut "$DESKTOP\PS3 Media Server.lnk" "$INSTDIR\pms.exe"
SectionEnd

Section "Uninstall"
  SetShellVarContext all

  Delete /REBOOTOK "$INSTDIR\Uninst.exe"
  RMDir /R /REBOOTOK "$INSTDIR\plugins"
  RMDir /R /REBOOTOK "$INSTDIR\renderers"
  RMDir /R /REBOOTOK "$INSTDIR\documentation"
  RMDir /R /REBOOTOK "$INSTDIR\win32"
  Delete /REBOOTOK "$INSTDIR\PMS.exe"
  Delete /REBOOTOK "$INSTDIR\PMS.bat"
  Delete /REBOOTOK "$INSTDIR\pms.jar"
  Delete /REBOOTOK "$INSTDIR\MediaInfo.dll"
  Delete /REBOOTOK "$INSTDIR\MediaInfo64.dll"
  Delete /REBOOTOK "$INSTDIR\CHANGELOG"
  Delete /REBOOTOK "$INSTDIR\WEB.conf"
  Delete /REBOOTOK "$INSTDIR\README"
  Delete /REBOOTOK "$INSTDIR\LICENSE.txt"
  Delete /REBOOTOK "$INSTDIR\debug.log"
  Delete /REBOOTOK "$INSTDIR\logback.xml"
  Delete /REBOOTOK "$INSTDIR\icon.ico"
  RMDir /REBOOTOK "$INSTDIR"

  Delete /REBOOTOK "$DESKTOP\PS3 Media Server.lnk"
  RMDir /REBOOTOK "$SMPROGRAMS\PS3 Media Server"
  Delete /REBOOTOK "$SMPROGRAMS\PS3 Media Server\PS3 Media Server.lnk"
  Delete /REBOOTOK "$SMPROGRAMS\PS3 Media Server\PS3 Media Server (Select Profile).lnk"
  Delete /REBOOTOK "$SMPROGRAMS\PS3 Media Server\Uninstall.lnk"
 
  DeleteRegKey HKEY_LOCAL_MACHINE "${REG_KEY_UNINSTALL}"
  DeleteRegKey HKCU "${REG_KEY_SOFTWARE}"

  nsSCM::Stop "PS3 Media Server"
  nsSCM::Remove "PS3 Media Server"
SectionEnd
