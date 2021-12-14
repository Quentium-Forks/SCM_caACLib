/*
FILENAME: ButtonFactory

DESCRIPTION: Factory class

AUTHOR: Marinus Klaassen (2021Q4)

EXAMPLE:
ButtonFactory.createInstance()
*/

ButtonFactory {

	*createInstance { |caller, class, buttonString1, buttonString2|
		var newInstance = Button();
		var colorPallete =  QPalette.auto;
		class = class.asString();
		newInstance.string = buttonString1;

		if (caller.class.asString == "PresetView" , {
		    newInstance.maxWidth = 45;
		});
		if (class == "btn-danger", {
			colorPallete.button = Color.red.alpha_(0.5);
			colorPallete.buttonText = Color.black;
			newInstance.palette_(colorPallete);
		});
		if (class == "btn-warning", {
			colorPallete.button = Color.new255(255,140,0).alpha_(0.5);
		    colorPallete.buttonText = Color.black;
		    newInstance.palette_(colorPallete);
        });
		if (class == "btn-success", {
			colorPallete.button = Color.green.alpha_(0.5);
		    colorPallete.buttonText = Color.black;
			newInstance.palette_(colorPallete);
		});
		if (class == "btn-primary", {
			colorPallete.button = Color.blue.alpha_(0.5);
		    colorPallete.buttonText = Color.white;
			newInstance.palette_(colorPallete);
		});
		if (class == "btn-next",{
			newInstance.states = [["+", Color.black, Color.black.alpha_(0.2)]];
			newInstance.maxWidth = 20;
		});
		if (class == "btn-previous",{
			newInstance.states = [["-", Color.black, Color.black.alpha_(0.2)]];
			newInstance.maxWidth = 20;
		});
		if (class.contains("btn-toggle"), {
			newInstance.states = [[buttonString1], [buttonString2]];
		});
		if (class == "btn-toggle-midiswitch", {
			newInstance.maxWidth = 30;
			newInstance.states = [["mb", Color.black, Color.clear.alpha_(0.1)], ["mb"]];
			newInstance.toolTip = "MIDI binding";
		});
		if (class == "btn-toggle-midiinvert", {
			newInstance.fixedSize = 16;
			newInstance.states = [["ø", Color.black, Color.clear.alpha_(0.1)], ["ø"]];
			newInstance.toolTip = "Invert MIDI input";
		});
		if (class == "btn-add", {
			newInstance = AddButton();
			newInstance.fixedHeight = 30;
			newInstance.fixedWidth = 70;
		});
		if (class.contains("btn-large"),{
			newInstance.font = Font("Menlo", 14);
			newInstance.minHeight = 60;
		});
		if (class.contains("toggle-play-patternboxprojectitemview"), {
			newInstance.font = Font("Menlo", 14);
            newInstance.minWidth_(50).maxWidth_(45).minHeight_(50);
			newInstance.states_([["PLAY"], ["STOP"]]);
		});
		if (class.contains("btn-patternboxprojectitemview-showpatternbox"), {
			newInstance.font = Font("Menlo", 14);
            newInstance.minWidth_(50).maxWidth_(45).minHeight_(50);
			newInstance.states_([["SCORE"]])
		});
		if (class.contains("btn-delete"), {
			newInstance = DeleteButton();
            newInstance.fixedSize_(10)
		});
		^newInstance;
	}
}