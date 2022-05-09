/*
FILENAME: ProjectPersistanceView

DESCRIPTION: ProjectPersistanceView save and load SC objects as archive files

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
ProjectPersistanceView(bounds:400@50).front()
*/

ProjectPersistanceView : View {
	var <eventLoadProject, <>actionChanged, <>actionClearAll, <>actionNewItem, <eventSaveProject, fileMenu, utilitiesMenu, buttonShowUtilitiesMenu, buttonShowSessionMenu, sessionMenu, <buttonShowFileMenu, <buttonSave, <buttonSaveAs, <labelProjectfile, <mainLayout, <>data, <>projectfile, <>contextID;

	*new { |contextID, parent, bounds|
		^super.new(parent, bounds).initialize(contextID);
	}

	initialize { |contextID|
		this.contextID = contextID;
		this.intializeView();
		this.initializeEvents();
	}

	intializeView {
		mainLayout = GridLayout();
		this.layout = mainLayout;

		buttonShowFileMenu = ButtonFactory.createInstance(this, class: "btn-normal", buttonString1: "File")
		.action_({ fileMenu.front; });

		mainLayout.add(buttonShowFileMenu, 0, 0);

		buttonShowSessionMenu = ButtonFactory.createInstance(this, class: "btn-normal", buttonString1: "Session")
		.action_({ sessionMenu.front; });

		mainLayout.add(buttonShowSessionMenu, 0, 1);

		buttonShowUtilitiesMenu = ButtonFactory.createInstance(this, class: "btn-normal", buttonString1: "Utilities")
		.action_({ utilitiesMenu.front; });

		mainLayout.add(buttonShowUtilitiesMenu, 0, 2);

		fileMenu = Menu(
			MenuAction("Open")
			.action_({ this.load(); }),
			MenuAction("Save")
			.action_({ this.save(); }),
			MenuAction("Save as")
			.action_({ this.saveAs(); })
		);

		sessionMenu = Menu(
			MenuAction("New item")
			.action_({ if (actionNewItem.notNil, { actionNewItem.value(this); }); }),
			MenuAction("Clear all")
			.action_({  if (actionClearAll.notNil, { actionClearAll.value(this); });}),
		);

		utilitiesMenu = Menu(
			MenuAction("SynthDef browser")
			.action_({ SynthDescLib.global.browse; }),
			MenuAction("Print control specs")
			.action_({ Spec.specs keysValuesDo: { |spec| postln(spec); }; }),
		);

		labelProjectfile = TextFieldFactory.createInstance(this);
		labelProjectfile.visible_(false);
		labelProjectfile.enabled_(false);
		mainLayout.addSpanning(labelProjectfile, row: 1, columnSpan: 4);

	}

	setProjectfile { |projectfile|
		this.projectfile = projectfile;
		labelProjectfile.string = projectfile;
		if (actionChanged.notNil, { actionChanged.value(this); });
	}

	initializeEvents {
		eventLoadProject = ();
		eventSaveProject = ();
	}

	invokeEvent { |event|
		event.changed(this);
	}

	saveAs {
		Dialog.savePanel({ |filepath|
			var stateFile = this.stateFile();
			var state = Dictionary();
			var pathName = PathName(filepath.value);
			this.invokeEvent(this.eventSaveProject);
			this.data.writeArchive(pathName.fullPath);
			this.setProjectfile(pathName.fullPath);
			state[\projectFilepath] = pathName.fullPath;
			state.writeArchive(stateFile);
			"project saved".postln;
		},{ "cancelled".postln; });
	}

	save {
		if (File.exists(this.projectfile), {
			this.data.writeArchive(this.projectfile + ".archive");
			this.invokeEvent(this.eventSaveProject);
			this.data.writeArchive(this.projectfile);
		},{
			this.saveAs();
		});
	}

	load {
		Dialog.openPanel({ |filepath|
			var pathName = PathName(filepath.value);
			this.loadData(Object.readArchive(pathName.fullPath));
			this.setProjectfile(pathName.fullPath);
		},{ "cancelled".postln; });
	}

	loadData { |data|
		this.data = data;
		this.invokeEvent(this.eventLoadProject);
	}

	stateFile {
		var stateDir = Platform.userAppSupportDir ++ "/ExtensionsWorkdir/SCM_CaACLib/" ++ this.class.asString ++ "/";
		var stateFile = stateDir ++ this.contextID ++ ".state";
		File.mkdir(stateDir); // Create if not exists.
		^stateFile;
	}

	autoLoad {
		var data, stateFile = this.stateFile();
		if (File.exists(stateFile), {
			var state = Object.readArchive(stateFile);
			if (state[\projectFilepath].notNil && File.exists(state[\projectFilepath]), {
				data = Object.readArchive(state[\projectFilepath]);
				data.postln;
				this.loadData(data);
				this.setProjectfile(state[\projectFilepath]);
			});
		});
	}
}

