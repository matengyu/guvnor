/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.guvnor.client.asseteditor.drools.modeldriven.ui;

import org.drools.guvnor.client.asseteditor.drools.modeldriven.HumanReadable;
import org.drools.guvnor.client.common.ClickableLabel;
import org.drools.guvnor.client.common.DirtyableFlexTable;
import org.drools.guvnor.client.common.DirtyableHorizontalPane;
import org.drools.guvnor.client.common.FormStylePopup;
import org.drools.guvnor.client.common.ImageButton;
import org.drools.guvnor.client.messages.Constants;
import org.drools.guvnor.client.resources.DroolsGuvnorImageResources;
import org.drools.guvnor.client.resources.DroolsGuvnorImages;
import org.drools.ide.common.client.modeldriven.SuggestionCompletionEngine;
import org.drools.ide.common.client.modeldriven.brl.FactPattern;
import org.drools.ide.common.client.modeldriven.brl.FromCompositeFactPattern;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class FromCompositeFactPatternWidget extends RuleModellerWidget {

    protected FromCompositeFactPattern pattern;
    protected DirtyableFlexTable       layout;
    protected boolean                  readOnly;

    public FromCompositeFactPatternWidget(RuleModeller modeller,
                                          EventBus eventBus,
                                          FromCompositeFactPattern pattern) {
        this( modeller,
              eventBus,
              pattern,
              null );
    }

    public FromCompositeFactPatternWidget(RuleModeller modeller,
                                          EventBus eventBus,
                                          FromCompositeFactPattern pattern,
                                          Boolean readOnly) {
        super( modeller,
               eventBus );
        this.pattern = pattern;

        //if readOnly is null, the readOnly attribute is calculated.
        if ( readOnly == null ) {
            this.calculateReadOnly();
        } else {
            this.readOnly = readOnly;
        }

        this.layout = new DirtyableFlexTable();
        if ( this.readOnly ) {
            this.layout.addStyleName( "editor-disabled-widget" );
        }
        this.layout.addStyleName( "model-builderInner-Background" );

        doLayout();
        initWidget( layout );
    }

    protected void doLayout() {

        int row = 0;
        if ( pattern.getFactPattern() != null ) {
            FactPattern fact = pattern.getFactPattern();
            if ( fact != null ) {
                this.layout.setWidget( row++,
                                       0,
                                       createFactPatternWidget( fact ) );
            }
        }

        this.layout.setWidget( row++,
                               0,
                               getCompositeLabel() );

    }

    private Widget createFactPatternWidget(FactPattern fact) {
        FactPatternWidget factPatternWidget;
        if ( this.readOnly ) {
            //creates a new read-only FactPatternWidget
            factPatternWidget = new FactPatternWidget( this.getModeller(),
                                                       this.getEventBus(),
                                                       fact,
                                                       false,
                                                       true );
            //this.layout.setWidget( 0, 0, factPatternWidget );
            return factPatternWidget;
        } else {
            factPatternWidget = new FactPatternWidget( this.getModeller(),
                                                       this.getEventBus(),
                                                       fact,
                                                       true,
                                                       false );
            factPatternWidget.addOnModifiedCommand( new Command() {
                public void execute() {
                    setModified( true );
                }
            } );
            //this.layout.setWidget( 0, 0, addRemoveButton( factPatternWidget, createClickHandlerForAddRemoveButton() ) );
            return addRemoveButton( factPatternWidget,
                                    createClickHandlerForAddRemoveButton() );
        }
    }

    private ClickHandler createClickHandlerForAddRemoveButton() {
        return new ClickHandler() {

            public void onClick(ClickEvent event) {
                if ( Window.confirm( Constants.INSTANCE.RemoveThisEntireConditionQ() ) ) {
                    setModified( true );
                    pattern.setFactPattern( null );
                    getModeller().refreshWidget();
                }

            }
        };
    }

    protected Widget getCompositeLabel() {

        ClickHandler click = new ClickHandler() {

            public void onClick(ClickEvent event) {
                Widget w = (Widget) event.getSource();
                showFactTypeSelector( w );

            }
        };
        String lbl = "<div class='form-field'>" + HumanReadable.getCEDisplayName( "from" ) + "</div>";

        DirtyableFlexTable panel = new DirtyableFlexTable();

        int r = 0;

        if ( pattern.getFactPattern() == null ) {
            panel.setWidget( r,
                             0,
                             new ClickableLabel( "<br> <font color='red'>" + Constants.INSTANCE.clickToAddPatterns() + "</font>",
                                                 click,
                                                 !this.readOnly ) );
            r++;
        }

        panel.setWidget( r,
                         0,
                         new HTML( lbl ) );
        ExpressionBuilder expressionBuilder = new ExpressionBuilder( this.getModeller(),
                                                                     this.getEventBus(),
                                                                     this.pattern.getExpression(),
                                                                     this.readOnly );
        expressionBuilder.addOnModifiedCommand( new Command() {
            public void execute() {
                setModified( true );
            }
        } );
        panel.setWidget( r,
                         1,
                         expressionBuilder );

        return panel;
    }

    /**
     * Pops up the fact selector.
     */
    protected void showFactTypeSelector(final Widget w) {
        SuggestionCompletionEngine completions = this.getModeller().getSuggestionCompletions();
        final ListBox box = new ListBox();
        String[] facts = completions.getFactTypes();

        box.addItem( Constants.INSTANCE.Choose() );

        for ( int i = 0; i < facts.length; i++ ) {
            box.addItem( facts[i] );
        }
        box.setSelectedIndex( 0 );

        final FormStylePopup popup = new FormStylePopup();
        popup.setTitle( Constants.INSTANCE.NewFactPattern() );
        popup.addAttribute( Constants.INSTANCE.chooseFactType(),
                            box );
        box.addChangeHandler( new ChangeHandler() {

            public void onChange(ChangeEvent event) {
                pattern.setFactPattern( new FactPattern( box.getItemText( box.getSelectedIndex() ) ) );
                setModified( true );
                getModeller().refreshWidget();
                popup.hide();
            }
        } );

        popup.show();
    }

    protected Widget addRemoveButton(Widget w,
                                     ClickHandler listener) {
        DirtyableHorizontalPane horiz = new DirtyableHorizontalPane();

        final Image remove= DroolsGuvnorImages.INSTANCE.DeleteItemSmall();
        remove.setAltText( Constants.INSTANCE.RemoveThisBlockOfData() );
        remove.setTitle( Constants.INSTANCE.RemoveThisBlockOfData() );
        remove.addClickHandler( listener );

        horiz.setWidth( "100%" );
        w.setWidth( "100%" );

        horiz.add( w );
        if ( !this.readOnly ) {
            horiz.add( remove );
        }
        return horiz;
    }

    public boolean isDirty() {
        return layout.hasDirty();
    }

    protected void calculateReadOnly() {
        if ( this.pattern.factPattern != null ) {
            this.readOnly = !this.getModeller().getSuggestionCompletions().containsFactType( this.pattern.factPattern.getFactType() );
        }
    }

    @Override
    public boolean isReadOnly() {
        return this.readOnly;
    }
}
