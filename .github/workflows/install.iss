#define MyAppName "ArcherBC2_testing"
#define MyAppVersion "2.0"
#define MyAppPublisher "Thermal Vision Technologies LLC"
#define MyAppURL "https://github.com/JAremko/ArcherBC2_testing"
#define MyAppSrc GetEnv("SRC")
#define MyAppExeName "ArcherBC2_testing.exe"

[Setup]
AppId=B2FDF84F-18BD-42B8-BD20-E645771D2911
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={userappdata}\ArcherBC2_testing
ChangesAssociations=no
DisableProgramGroupPage=yes
PrivilegesRequired=lowest
OutputBaseFilename=ArcherBC2_install_testing
OutputDir="{#MyAppSrc}"
Compression=lzma
SolidCompression=yes
WizardStyle=modern

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "ukrainian"; MessagesFile: "compiler:Languages\Ukrainian.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "{#MyAppSrc}\{#MyAppExeName}"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#MyAppSrc}\profedit.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#MyAppSrc}\update.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#MyAppSrc}\update.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#MyAppSrc}\runtime\*"; DestDir: "{app}\runtime"; Flags: ignoreversion recursesubdirs createallsubdirs
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{userdesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

[UninstallDelete]
Type: filesandordirs; Name: "{app}"
