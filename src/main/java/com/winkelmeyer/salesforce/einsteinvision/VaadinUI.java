package com.winkelmeyer.salesforce.einsteinvision;

import java.util.Set;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.winkelmeyer.salesforce.einsteinvision.ext.AccessTokenProvider;
import com.winkelmeyer.salesforce.einsteinvision.ext.AccessTokenRefresher;
import com.winkelmeyer.salesforce.einsteinvision.ext.representations.AccessToken;
import com.winkelmeyer.salesforce.einsteinvision.model.Dataset;
import com.winkelmeyer.salesforce.einsteinvision.model.Example;
import com.winkelmeyer.salesforce.einsteinvision.model.PredictionResult;
import com.winkelmeyer.salesforce.einsteinvision.model.Probability;

@SpringUI
public class VaadinUI extends UI {

	private Image img;
	private AccessToken accessToken;

	@Override
	protected void init(VaadinRequest vaadinRequest) {
		initAccessToken();
		setContent(buildMainLayout());
	}
	
	private void initAccessToken() {
	    String email = System.getenv("EINSTEIN_VISION_ACCOUNT_ID");
	    String privateKey = System.getenv("EINSTEIN_VISION_PRIVATE_KEY");

	    long durationInSeconds = 60 * 15;
	    
	    AccessTokenProvider tokenProvider = AccessTokenProvider.getProvider(email, privateKey, durationInSeconds);
		
	    AccessTokenRefresher.schedule(tokenProvider, 60 * 14);
	    
	    accessToken = tokenProvider.getAccessToken();
	}
	
	private VerticalLayout buildMainLayout() {
		VerticalLayout layout = new VerticalLayout();
		
		Label header = new Label();
		header.setWidth("400px");
		header.setValue("<br />This example application allows to predict a remote URL image based on the General Image Classifier. You can also check out trained examples from the 'Beaches and Mountains' demonstration. <br /><br />");
		header.setContentMode(ContentMode.HTML);
		
		TabSheet tabSheet = new TabSheet();
		tabSheet.addStyleName("equal-width");
		
		tabSheet.addTab(buildPredictionLayout(), "Predict Remote URL");
		tabSheet.addTab(buildExampleGrid(), "List Beach and Mountain Examples");
		
		layout.addComponents(header, tabSheet);
		return layout;
	}
	
	private VerticalLayout buildPredictionLayout() {
		VerticalLayout layout = new VerticalLayout();
		
		Label br = new Label("<br />");
		br.setContentMode(ContentMode.HTML);
		
		TextField fieldUrl = new TextField();
		fieldUrl.setWidth("500px");
		Button btnPredict = new Button("Predict Image from url");
		btnPredict.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				String url = fieldUrl.getValue();
				if (url.isEmpty()) {
					return;
				}
				PredictionService service = new PredictionService(accessToken.getToken());
				try {
					PredictionResult result = service.predictUrl("GeneralImageClassifier", url, "");
					for (Probability prob : result.getProbabilities()) {
						layout.addComponent(new Label(prob.getLabel() + " (" + prob.getProbability() + ")" ));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		});
		layout.addComponents(br, fieldUrl, btnPredict);
		return layout;
	}

	private HorizontalLayout buildExampleGrid() {
		HorizontalLayout layout = new HorizontalLayout();
		
		PredictionService service = new PredictionService(accessToken.getToken());
		Grid<Example> grid = new Grid<>();
		
		try {
			
			Dataset[] datasets = service.getDatasets();
			Example[] examples = service.getExamples(datasets[0]);
			grid.setItems(examples);
			grid.addColumn(Example::getName).setCaption("Name");

			grid.addSelectionListener(event -> {
				if (img!=null) {
					layout.removeComponent(img);
				}
				Set<Example> selected = event.getAllSelectedItems();
				Example ex = selected.iterator().next();
				ExternalResource source = new ExternalResource(ex.getLocation());
				img = new Image(ex.getName(), source);
				img.setHeight("400px");
				layout.addComponent(img);
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
		layout.addComponents(grid);
		return layout;

	}



}