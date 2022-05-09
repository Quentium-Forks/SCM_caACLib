/*
FILENAME: PatternBoxProjectView

DESCRIPTION: Maintains a project of PatternBox instances.

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
s.boot;
PatternBoxProjectView(bounds:400@700).front();
*/

PatternBoxProjectView : View {
	var patternBoxProjectItemViews, lemurClient, <eventAddPatternBox;
	var mainLayout, footerLayout, projectSaveAndLoadView, menuFile, layoutPatternBoxItems, scrollViewPatternBoxItems, buttonAddPatternBox, layoutHeader, serverControlView, tempoClockView;

	*new { |parent, bounds, lemurClient|
		^super.new(parent, bounds).initialize(lemurClient);
	}

	initialize { |lemurClient|
		patternBoxProjectItemViews = List();
		lemurClient = lemurClient;
		this.initializeEvents();
		this.initializeView();
		this.registerEventHandlers();
	    projectSaveAndLoadView.autoLoad();
	}

	initializeView {
		this.name = "PatternBox Project";
		this.deleteOnClose = false;

		mainLayout = VLayout();
		this.layout = mainLayout;

		projectSaveAndLoadView = ProjectPersistanceViewFactory.createInstance(this, contextID: "PatternBoxProjectView");
		projectSaveAndLoadView.actionChanged = { |sender| this.name = "PatternBox Project: " ++ PathName(sender.projectfile).fileName; };
		projectSaveAndLoadView.actionClearAll = { this.clearAll(); };
		projectSaveAndLoadView.actionNewItem = { this.invokeEvent(this.eventAddPatternBox); };
		mainLayout.add(projectSaveAndLoadView);

		serverControlView = ServerControlViewFactory.createInstance(this);
		mainLayout.add(serverControlView);

		layoutPatternBoxItems = VLayout([nil, stretch:1, align: \bottom]); // workaround. insert before stretchable space.
		layoutPatternBoxItems.margins = 0!4;
		layoutPatternBoxItems.spacing = 5;

		scrollViewPatternBoxItems = ScrollViewFactory.createInstance(this);
		scrollViewPatternBoxItems.canvas.layout = layoutPatternBoxItems;
		scrollViewPatternBoxItems.background = Color.black.alpha_(0.1);
		scrollViewPatternBoxItems.canvas.canReceiveDragHandler = {  |view, x, y|
			View.currentDrag.isKindOf(PatternBoxProjectItemView);
		};

		scrollViewPatternBoxItems.canvas.receiveDragHandler = { |view, x, y|
			var targetPosition = patternBoxProjectItemViews.size - 1;
			patternBoxProjectItemViews.remove(View.currentDrag);
			patternBoxProjectItemViews.insert(targetPosition, View.currentDrag);
			layoutPatternBoxItems.insert(View.currentDrag, targetPosition);
		};

		mainLayout.add(scrollViewPatternBoxItems);

		footerLayout = HLayout();
		footerLayout.margins = 0!4;

		tempoClockView = TempoClockViewFactory.createInstance(this);
		footerLayout.add(tempoClockView,  align: \left);

		buttonAddPatternBox = ButtonFactory.createInstance(this, "btn-add");
		buttonAddPatternBox.toolTip = "Add a new PatternBox.";
		buttonAddPatternBox.action = { this.invokeEvent(this.eventAddPatternBox); };

		footerLayout.add(buttonAddPatternBox,  align: \right);

		mainLayout.add(footerLayout);
	}

	invokeEvent { |event|
		event.changed(this);
	}

	initializeEvents {
		eventAddPatternBox = ();
	}

	registerEventHandlers {
		eventAddPatternBox.addDependant({
			this.addPatternBox();
		});
		projectSaveAndLoadView.eventLoadProject.addDependant({  | event, sender /* projectSaveAndLoadView */ |
			this.loadState(sender.data);
		});
		projectSaveAndLoadView.eventSaveProject.addDependant({  | event, sender /* projectSaveAndLoadView */ |
			sender.data = this.getState();
		});
	}

	addPatternBox { |positionInLayout, sourcePatterBoxItemView, duplicate|
		var patternBoxProjectItemView = PatternBoxProjectItemView(lemurClient);
		if(duplicate == true, {
			var state = sourcePatterBoxItemView.getState();
			state[\patternBoxName] = state[\patternBoxName] + " - COPY";
			patternBoxProjectItemView.loadState(state);
		});

		patternBoxProjectItemView.actionRemove = { | sender |
			patternBoxProjectItemViews.remove(patternBoxProjectItemView);
		};

		patternBoxProjectItemView.actionInsertPatternBox = { |sender, insertType|
			var positionInLayout = patternBoxProjectItemViews.indexOf(sender);
			var duplicate = false;
			if (insertType == "INSERT_AFTER", {
				positionInLayout = positionInLayout + 1;
			});
			if (insertType == "INSERT_AFTER_DUPLICATIE", {
				positionInLayout = positionInLayout + 1;
				duplicate = true;
			});
			this.addPatternBox(positionInLayout, sender, duplicate);
		};

		patternBoxProjectItemView.actionMovePatternBox = { |dragDestinationObject, dragObject|
			var targetPosition;
			if (dragDestinationObject !==  dragObject, {
				targetPosition = patternBoxProjectItemViews.indexOf(dragDestinationObject);
				patternBoxProjectItemViews.remove(dragObject);
				patternBoxProjectItemViews.insert(targetPosition, dragObject);
				layoutPatternBoxItems.insert(dragObject, targetPosition);
			});
		};

		if (positionInLayout.notNil, {
			layoutPatternBoxItems.insert(patternBoxProjectItemView, positionInLayout);
			patternBoxProjectItemViews.insert(positionInLayout, patternBoxProjectItemView);
		},{
			layoutPatternBoxItems.insert(patternBoxProjectItemView, patternBoxProjectItemViews.size); // workaround. insert before stretchable space.
			patternBoxProjectItemViews.add(patternBoxProjectItemView);
		});
		^patternBoxProjectItemView;
	}

	clearAll {
		patternBoxProjectItemViews.copy do: { | patternBox| patternBox.dispose(); };
	}

	getState {
		var state = Dictionary();
		state[\type] = "PatternBoxProjectView";
		state[\patternBoxProjectItemViewsStates] = patternBoxProjectItemViews.collect({ |patternBoxProjectItemView| patternBoxProjectItemView.getState(); });
		^state;
	}

	loadState{ |state|
		var patternBoxProjectItemView;
		if (state.isKindOf(Dictionary) && state[\type] == "PatternBoxProjectView",
			{
				// Remove the patternBoxViews that are to many.
				if (state[\patternBoxProjectItemViewsStates].size < patternBoxProjectItemViews.size, {
					var amountToMany = patternBoxProjectItemViews.size - state[\patternBoxProjectItemViewsStates].size;
					amountToMany do: {
						patternBoxProjectItemViews.pop().dispose();
					};
				});
				// Reuse existing patternBoxViews or add a new PatternBox.
				state[\patternBoxProjectItemViewsStates] do: { |state, position|
					if (patternBoxProjectItemViews[position].isNil, {
						patternBoxProjectItemView = this.addPatternBox();
					}, {
						patternBoxProjectItemView = patternBoxProjectItemViews[position];
					});
					patternBoxProjectItemView.loadState(state);
				};
		});
	}
}
