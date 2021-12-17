/*
FILENAME: PatternBoxParamControlGroupView

DESCRIPTION: Select an compose widgest.

AUTHOR: Marinus Klaassen (2012, 2021Q3)

a = PatternBoxParamControlGroupView().front;
a.bounds
*/

PatternBoxParamControlGroupView : View {

	var mainLayout, buttonAdd, <controlItems, <editMode, <>controlNameDefault, <>actionControlCRUD;

	*new { |parent, bounds|
		^super.new(parent, bounds).initialize();
	}

	initialize {
		controlNameDefault = "default";
		controlItems = List();
		this.initializeView();
	}

	initializeView {
		mainLayout = VLayout();
		this.layout = mainLayout;
		buttonAdd = ButtonFactory.createInstance(this, class: "btn-add");
		buttonAdd.fixedSize_(20);
		buttonAdd.toolTip = "Add a control";
		buttonAdd.action_({ this.onButtonClick_AddPatternBoxParamControlItemView() });
		buttonAdd.visible = false;
		editMode = false;
		mainLayout.add(buttonAdd, align: \right);
	}

	editMode_ { |mode|
		buttonAdd.visible  = mode;
		controlItems do: { |item| item.editMode = mode; };
		editMode = mode;
	}

	onButtonClick_AddPatternBoxParamControlItemView { |state|
		var controlItem = PatternBoxParamControlItemView(name: "control" ++ (controlItems.size + 1));
		controlItem.actionRemove = { |sender|
			controlItems.remove(sender);
			controlItem.remove();
			if (actionControlCRUD.notNil, { actionControlCRUD.value(this); });
		};
		controlItem.actionControlItemChanged = {
			if (actionControlCRUD.notNil, { actionControlCRUD.value(this); });
		};
		controlItem.actionControlNameChanged = {
			if (actionControlCRUD.notNil, { actionControlCRUD.value(this); });

		};
		if (state.notNil, { controlItem.loadState(state) });
		controlItems.add(controlItem);
		controlItem.editMode = editMode;
		mainLayout.insert(controlItem, controlItems.size - 1);
		if (actionControlCRUD.notNil, { actionControlCRUD.value(this); });
	}

	randomize {
		controlItems do: { |controlItem| controlItem.randomize(); };
	}

	getProxies {
		var result = Dictionary();
		controlItems do: { |item| result.putAll(item.getProxies()); };
		^result;
	}

	getState {
		var state = Dictionary();
		state[\visible] = this.visible;
		state[\editMode] = editMode;
		state[\controlItems] = controlItems collect: { |item| item.getState(); };
			state[\controlItems].postln;
		^state;
	}

	loadState { |state|
		controlItems do: { |item| item.remove; };
		controlItems = List();
		if (state.notNil, {
		if(state[\editMode].notNil, { this.editMode = state[\editMode]; });
		if(state[\visible].notNil, { this.visible = state[\visible]; });
		state[\controlItems] do: { |itemState|
			this.onButtonClick_AddPatternBoxParamControlItemView(itemState);
		};
		}, {  this.editMode  = false; });
	}
}


