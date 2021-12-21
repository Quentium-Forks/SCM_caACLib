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
	var mainLayout, footerLayout, projectSaveAndLoadView, layoutPatternBoxItems, scrollViewPatternBoxItems, buttonAddPatternBox, layoutHeader, serverControlView, tempoClockView;

	*new { |parent, bounds, lemurClient|
		^super.new(parent, bounds).initialize(lemurClient);
	}

	initialize { |lemurClient|
		patternBoxProjectItemViews = List();
		lemurClient = lemurClient;
		this.initializeEvents();
		this.initializeView();
		this.registerEventHandlers();
	}

	initializeView {
		this.name = "PatternBox Project";
		this.deleteOnClose = false;

		mainLayout = VLayout();
		this.layout = mainLayout;

		projectSaveAndLoadView = ProjectPersistanceViewFactory.createInstance(this);
		mainLayout.add(projectSaveAndLoadView);

		serverControlView = ServerControlViewFactory.createInstance(this);
		mainLayout.add(serverControlView);

		layoutPatternBoxItems = VLayout([nil, stretch:1, align: \bottom]); // workaround. insert before stretchable space.
		layoutPatternBoxItems.margins = 0!4;
		layoutPatternBoxItems.spacing = 2;

		scrollViewPatternBoxItems = ScrollViewFactory.createInstance(this);
		scrollViewPatternBoxItems.canvas.layout = layoutPatternBoxItems;
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

	addPatternBox {
		var patternBoxProjectItemView = PatternBoxProjectItemView(lemurClient);
		patternBoxProjectItemView.actionRemove = { | sender |
			patternBoxProjectItemViews.remove(patternBoxProjectItemView);
		};
		patternBoxProjectItemView.actionMoveDown = { |sender|
			this.movePatternBoxItemView(sender, 1);
		};
		patternBoxProjectItemView.actionMoveUp = { |sender|
			this.movePatternBoxItemView(sender, -1);
		};
		layoutPatternBoxItems.insert(patternBoxProjectItemView, patternBoxProjectItemViews.size); // workaround. insert before stretchable space.
		patternBoxProjectItemViews.add(patternBoxProjectItemView);
		^patternBoxProjectItemView;
	}

	movePatternBoxItemView { |patternBoxProjectItemView, step|
		var currentPosition = patternBoxProjectItemViews.indexOf(patternBoxProjectItemView);
		var nextPosition = currentPosition + step;
		if (nextPosition >= 0 && (nextPosition < patternBoxProjectItemViews.size), {
			patternBoxProjectItemViews.removeAt(currentPosition);
			patternBoxProjectItemViews.insert(nextPosition, patternBoxProjectItemView);
			layoutPatternBoxItems.insert(patternBoxProjectItemView, nextPosition);
		});
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
