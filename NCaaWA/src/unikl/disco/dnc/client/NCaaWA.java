package unikl.disco.dnc.client;




import java.util.ArrayList;
import java.util.LinkedList;

import unikl.disco.dnc.client.demos.Demo1;
import unikl.disco.dnc.client.demos.Demo2;
import unikl.disco.dnc.client.demos.Demo3;
import unikl.disco.dnc.client.demos.Demo4;
import unikl.disco.dnc.shared.Configuration;
import unikl.disco.dnc.shared.curves.ArrivalCurve;
import unikl.disco.dnc.shared.curves.ServiceCurve;
import unikl.disco.dnc.shared.network.Flow;
import unikl.disco.dnc.shared.network.Link;
import unikl.disco.dnc.shared.network.Network;
import unikl.disco.dnc.shared.network.Server;
import unikl.disco.dnc.shared.results.PmooAnalysisResults;
import unikl.disco.dnc.shared.results.SeparateFlowAnalysisResults;
import unikl.disco.dnc.shared.results.TotalFlowAnalysisResults;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.TabBar;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TabBar.Tab;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.server.rpc.RPCRequest;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class NCaaWA implements EntryPoint {
	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
   

	private final DiscoDNCAnalysisAsync ncaawa = GWT
			.create(DiscoDNCAnalysis.class);
	int cnt=0;
	int total_servers;
	Boolean link= false;
	Boolean choice=false;
  
	   
	final Label serversLabel = new Label("Servers:");
	final Label serversValueLabel = new Label();
	final Label flowOfInterestLabel = new Label("Flow of Interest:");
	final Label arrivalCurveLabel = new Label("Arrival Curve:");
	final Label arrivalCurveValueLabel = new Label();
	final Label serviceCurveLabel = new Label("Service Curve:");
	final Label serviceCurveValueLabel = new Label();
	final Label serviceCurveMaxLabel =new  Label("Max Service Curve:");
	final Label serviceCurveMaxValueLabel = new Label();
	final Label flowOfInterestValueLabel = new Label();
	final Label perServerTFALabel = new Label("per Server:");
	final Label perServerTFAValueLabel = new Label();
	final Label e2eSFASCLabel = new Label("e2eSFA SC:");
	final Label e2eSFASCValueLabel = new Label();
	final Label perServerSFALabel = new Label("per Server:");
	final Label perServerSFAValueLabel = new Label();
	final Label e2ePMOOSFASCLabel = new Label("e2ePMOO SFA SC:");
	final Label e2ePMOOSFASCValueLabel = new Label();
	
	
	final Grid inputValuesGrid = new Grid(5,2);
	final Grid resultsfaValuesGrid = new Grid (4,2);
	final Grid resulttfaValuesGrid = new Grid(3,2);
	final Grid resultpmooValuesGrid = new Grid(3,2);
	final Label delayLabel = new Label("Delay Bound: ");
	final Label delayBoundLabel = new Label();
	final Label backlogLabel = new Label("Backlog Bound: ");
	final Label backlogBoundLabel = new Label();
	final Label delaysfaLabel = new Label("Delay Bound: ");
	final Label delayBoundsfaLabel = new Label();
	final Label backlogsfaLabel = new Label("Backlog Bound: ");
	final Label backlogBoundsfaLabel = new Label();
	
	final SplitLayoutPanel sfaSplitLayoutPanel= new SplitLayoutPanel();
	
	VerticalPanel verticalPanel= new VerticalPanel();
	Label inputLabel = new Label("Input values");
	Label resultLabel = new Label("Analysis Results");
	Label TFAresultsLabel= new Label("TFA Result:");
	Label PMOOresultsLabel= new Label("PMOO Result:");
	Label SFAresultsLabel= new Label("SFA Result:");
	
	//User network
	final Label arrCurveLabel= new Label("Enter Arrival curve value:");
	final Label rateArrCurveLabel= new Label("Enter Rate for Arrival Curve:");
	final Label rateValueArrCurveLabel= new Label("");
	final Label burstinessArrCurveLabel = new Label("Enter Burstiness for Arrival Curve:");
	final Label burstinessValueArrCurveLabel = new Label("");
	final Label serCurveLabel= new Label("Enter Service curve value:");
	final Label rateSerCurveLabel= new Label("Enter Rate for Service Curve:");
	final Label rateValueSerCurveLabel= new Label("");
	final Label latencySerCurveLabel = new Label("Enter Latency for Service Curve:");
	final Label latencyValueSerCurveLabel= new Label("");
	final Label serCurveMaxLabel= new Label("Enter Max Service curve value:");
	final Label rateserCurveMaxLabel= new Label("Enter Rate for Max Service Curve:");
	final Label rateValueSerCurveMaxLabel= new Label("");
	final Label latencySerCurveMaxLabel = new Label("Enter Latency for Max Service Curve:");
	final Label latencyValueSerCurveMaxLabel= new Label("");
	
	
	public Network network;
	public Configuration configuration;
	public Flow flow_of_interest;
	CheckBox pathSelCheckBox= new CheckBox("Give Source and Sink Servers");
	TextBox sourceBox= new TextBox();
	TextBox sinkBox  = new TextBox();
	Button demo11=new Button();
	Button demo12=new Button();
	Button demo13=new Button();
	Button demo14=new Button();
	Label dummyLabel= new Label();
	TextBox service_rate_textbox=new TextBox();
	TextBox service_latency_textbox=new TextBox();
	TextBox arrival_rate_textbox=new TextBox();
	TextBox arrival_burst_textbox=new TextBox();
	Button b=new Button();		
	Button clearContentButton = new Button();
	
	
	int linkCount=0;
	Label serverResponseLabel= new Label();
	ArrayList<String> serversList= new ArrayList<String>();
	
	public void onModuleLoad() {
				
		b=Button.wrap(Document.get().getElementById("calculation"));
		b.addClickHandler(new ResultHandler());
		
		clearContentButton= Button.wrap(Document.get().getElementById("clearContent"));
		clearContentButton.addClickHandler(new ClearContent_handler());
		
		demo11=Button.wrap(Document.get().getElementById("demo1"));		
		demo11.addClickHandler(new Demo1_handler());
		
		demo12=Button.wrap(Document.get().getElementById("demo2"));		
		demo12.addClickHandler(new Demo2_handler());
		
		demo13=Button.wrap(Document.get().getElementById("demo3"));		
		demo13.addClickHandler(new Demo3_handler());
		
		demo14=Button.wrap(Document.get().getElementById("demo4"));		
		demo14.addClickHandler(new Demo4_handler());
		
		}
	   
	
	void Result_template(){
		
		ServiceCurve service_curve = ServiceCurve.createRateLatency( 10.0e6, 0.01 );
		ServiceCurve max_service_curve = ServiceCurve.createRateLatency( 100.0e6, 0.001 );
		ArrivalCurve arrival_curve = ArrivalCurve.createTokenBucket( 0.1e6, 0.1 * 0.1e6 );
		
		Element arrival_curve_values=Document.get().getElementById("Arrival_curve");
		arrival_curve_values.setInnerHTML(arrival_curve.toString());
	
		Element servicecurve_values=Document.get().getElementById("Service_curve");
		servicecurve_values.setInnerHTML(service_curve.toString());
	
		Element max_servicecurve_values=Document.get().getElementById("Max_service_curve");
		max_servicecurve_values.setInnerHTML(max_service_curve.toString());
		
	}
	
	class ClearContent_handler implements ClickHandler{

		@Override
		public void onClick(ClickEvent event) {
			network = null;
			configuration= null;
			serversList.clear();
			RootPanel.get().remove(verticalPanel);
			
		}
		
	}
		
	class Demo1_handler implements ClickHandler
	{

		@Override
		public void onClick(ClickEvent event) 
		{
			Element title_of_demos=Document.get().getElementById("demo_title");
			title_of_demos.setInnerHTML("Demo 1");			
			
			try 
			{
				Demo1 demo1=new Demo1();
				Result_template(); 
				choice=false;
				requestPmooAnalysis( demo1.network, demo1.configuration, demo1.flow_of_interest );
				requestSfaAnalysis( demo1.network, demo1.configuration, demo1.flow_of_interest );
				requestTfaAnalysis( demo1.network, demo1.configuration, demo1.flow_of_interest );
				Element flow_of_interest_value= Document.get().getElementById("flow_of_intrest");
				flow_of_interest_value.setInnerHTML(demo1.getFlow_of_interest().toString());
				Element server_list_value= Document.get().getElementById("server");
				server_list_value.setInnerHTML(demo1.getServers().toString());
			}
			catch (Exception e) 
			{
				
				e.printStackTrace();
			}
		}
			
	}
	class Demo2_handler implements ClickHandler
	{

		@Override
		public void onClick(ClickEvent event) 
		{
			Element title_of_demos=Document.get().getElementById("demo_title");
			title_of_demos.setInnerHTML("Demo 2");
			
			try 
			{
				Demo2 demo2=new Demo2();
				Result_template();
				choice=false;
				requestPmooAnalysis( demo2.network, demo2.configuration, demo2.flow_of_interest );
				requestSfaAnalysis( demo2.network, demo2.configuration, demo2.flow_of_interest );
				requestTfaAnalysis( demo2.network, demo2.configuration, demo2.flow_of_interest );
				Element flow_of_interest_value= Document.get().getElementById("flow_of_intrest");
				flow_of_interest_value.setInnerHTML(demo2.getFlow_of_interest().toString());
				Element server_list_value= Document.get().getElementById("server");
				server_list_value.setInnerHTML(demo2.getServers().toString());

			}
			catch (Exception e) 
			{
				
				e.printStackTrace();
			}
		}		
	}
	class Demo3_handler implements ClickHandler
	{

		@Override
		public void onClick(ClickEvent event) 
		{
			Element title_of_demos=Document.get().getElementById("demo_title");
			title_of_demos.setInnerHTML("Demo 3");
			
			try 
			{
				Demo3 demo3=new Demo3();
				Result_template();
				choice=false;
				requestPmooAnalysis( demo3.network, demo3.configuration, demo3.flow_of_interest );
				requestSfaAnalysis( demo3.network, demo3.configuration, demo3.flow_of_interest );
				requestTfaAnalysis( demo3.network, demo3.configuration, demo3.flow_of_interest );
				Element flow_of_interest_value= Document.get().getElementById("flow_of_intrest");
				flow_of_interest_value.setInnerHTML(demo3.getFlow_of_interest().toString());
				Element server_list_value= Document.get().getElementById("server");
				server_list_value.setInnerHTML(demo3.getServers().toString());
			
			}
			catch (Exception e) 
			{
				
				e.printStackTrace();
			}
		}
		
	}
	class Demo4_handler implements ClickHandler
	{

		@Override
		public void onClick(ClickEvent event) 
		{
			Element title_of_demos=Document.get().getElementById("demo_title");
			title_of_demos.setInnerHTML("Demo 4");
			
			try 
			{
				Demo4 demo4=new Demo4();
				Result_template(); 
				choice=false;
				requestPmooAnalysis( demo4.network, demo4.configuration, demo4.flow_of_interest );
				requestSfaAnalysis( demo4.network, demo4.configuration, demo4.flow_of_interest );
				requestTfaAnalysis( demo4.network, demo4.configuration, demo4.flow_of_interest );
				Element flow_of_interest_value= Document.get().getElementById("flow_of_intrest");
				flow_of_interest_value.setInnerHTML(demo4.getFlow_of_interest().toString());
				Element server_list_value= Document.get().getElementById("server");
				server_list_value.setInnerHTML(demo4.getServersList().toString());

			}
			catch (Exception e) 
			{
				
				e.printStackTrace();
			}
		}
		
	}
	
	
	class ResultHandler implements ClickHandler
	{
		@Override
		public void onClick(ClickEvent event) 
		{
			ServiceCurve service_curve=null,max_service_curve=null;			
			ArrivalCurve arrival_curve=null;			
			
			max_service_curve = ServiceCurve.createRateLatency( 100.0e6, 0.001 );			
			
			service_rate_textbox=TextBox.wrap(Document.get().getElementById("service_curve_Rate"));
			service_latency_textbox=TextBox.wrap(Document.get().getElementById("service_curve_Latency"));
			arrival_rate_textbox=TextBox.wrap(Document.get().getElementById("arrival_curve_Rate"));
			arrival_burst_textbox=TextBox.wrap(Document.get().getElementById("arrival_curve_Burst"));
			
			if(service_latency_textbox.getText().equals("") && service_latency_textbox.getText().equals(""))
			{
				service_curve = ServiceCurve.createRateLatency( 10.0e6, 0.01 );
				
			}
			else
			{
				Double service_curve_rate_value=Double.parseDouble(service_rate_textbox.getText().toString());
				Double service_curve_latency_value=Double.parseDouble(service_latency_textbox.getText().toString());			
			
				service_curve= ServiceCurve.createRateLatency(service_curve_rate_value,service_curve_latency_value);				
								
			}	
			
			if(arrival_rate_textbox.getText().equals("") && arrival_burst_textbox.getText().equals(""))
			{
				arrival_curve = ArrivalCurve.createTokenBucket( 0.1e6, 0.1 * 0.1e6 );					
			}	
			else
			{	
				
				Double arrival_curve_rate_value=Double.parseDouble(arrival_rate_textbox.getText().toString());
				Double arrival_curve_burst_value=Double.parseDouble(arrival_burst_textbox.getText().toString());
			
				arrival_curve = ArrivalCurve.createTokenBucket(arrival_curve_rate_value,arrival_curve_burst_value);
			}	
							
			LinkedList<Link> path0 = new LinkedList<Link>();
			serversList.clear();
			network = new Network();	
			configuration = new Configuration();
			
			sourceBox=TextBox.wrap(Document.get().getElementById("source_server"));
			sinkBox=TextBox.wrap(Document.get().getElementById("sink_server"));
			
			//Getting values for servers from user created network 			    
			JSONValue jv_servers=JSONParser.parseLenient(Document.get().getElementById("nodes").getInnerText());			
			JSONArray jsonArray_servers= (JSONArray)jv_servers;
			total_servers=jsonArray_servers.size();
			System.out.println("totalservers:"+total_servers);
			
				
			Server[] servers = new Server[total_servers+1];
		
		try{
			for ( int i = 0; i <total_servers; i++ )
			{
				servers[i] = network.addServer( service_curve, max_service_curve );
				servers[i].setUseGamma( false );
				servers[i].setUseExtraGamma( false );
				serversList.add(servers[i].getAlias());
			}
			
			for(int i=0;i<total_servers;i++){
				if((Integer.parseInt(sourceBox.getText().toString())-1) == servers[i].getId()
					|| (Integer.parseInt(sinkBox.getText().toString())-1) == servers[i].getId()){
					
					cnt=cnt+1;
				}
			}
			
			if(cnt!=2)
			{
				Window.alert("Source/Sink entered is not part of network. Please enter source/sink only present in the network");
				cnt=0;
					
			}
			else
			{

				//get source and sink values
				//Getting values for links from user created network
				JSONValue jv_links=JSONParser.parseLenient(Document.get().getElementById("edges").getInnerText());
				JSONArray jsonArray_links= (JSONArray)jv_links;
				int links_size = jsonArray_links.size();
				Link[] links = new Link[links_size+1];
							
				//checking if path exists from source to sink
				String fromValidation="";
				String toValidation="";
				JSONObject linkSetJsonObject;
					for(int i=0;i<jsonArray_links.size();i++)
					{
							linkSetJsonObject= (JSONObject)jsonArray_links.get(i);
							 fromValidation =linkSetJsonObject.get("from").toString().replaceAll("\"","");
							if(fromValidation.equals(sourceBox.getText().toString()))
							{
								fromValidation=linkSetJsonObject.get("to").toString().replaceAll("\"", "");
							}
					}
					for(int i=0;i<jsonArray_links.size();i++)
					{
						 linkSetJsonObject= (JSONObject)jsonArray_links.get(i);
						 if(fromValidation.equals(linkSetJsonObject.get("from").toString().replaceAll("\"","")))
						 {
							 toValidation = linkSetJsonObject.get("to").toString().replaceAll("\"","");
						 }
						 toValidation = linkSetJsonObject.get("to").toString().replaceAll("\"","");
							if(toValidation.equals(sinkBox.getText().toString()))
							{
								System.out.print("Path from source to sink exists");
							}
							else{
								fromValidation=toValidation;
							}
					}
					
						//Getting link values from array 			
						try
							{
								for(int i=0;i<jsonArray_links.size();i++)
								{
									JSONObject req = (JSONObject)jsonArray_links.get(i);
									String from=req.get("from").toString().replaceAll("\"","");
									String to=req.get("to").toString().replaceAll("\"","");
									
									int from_links=Integer.parseInt(from);
									int to_links=Integer.parseInt(to);
									
									links[i] = network.addLink(servers[from_links-1], servers[to_links-1]);
									
															
								}
								
								
								int source=Integer.parseInt(sourceBox.getText().toString())-1;
								int sink=Integer.parseInt(sinkBox.getText().toString())-1;
								flow_of_interest=network.addFlow( arrival_curve, servers[source],servers[sink] );						
									
								choice=true;
								
								requestPmooAnalysis(network, configuration, flow_of_interest);	
								requestTfaAnalysis(network, configuration, flow_of_interest);
								requestSfaAnalysis(network, configuration, flow_of_interest);

								//results for user network
								flowOfInterestValueLabel.setText(flow_of_interest.toString());
								serversValueLabel.setText(serversList.toString());
								arrivalCurveValueLabel.setText(arrival_curve.toString());
								serviceCurveValueLabel.setText(service_curve.toString());
								serviceCurveMaxValueLabel.setText(max_service_curve.toString());
								inputValuesGrid.setWidget(0, 0, serversLabel);
								inputValuesGrid.setWidget(0, 1, serversValueLabel);
								inputValuesGrid.setWidget(1, 0, flowOfInterestLabel);
								inputValuesGrid.setWidget(1, 1, flowOfInterestValueLabel);
								inputValuesGrid.setWidget(2, 0, arrivalCurveLabel);
								inputValuesGrid.setWidget(2, 1, arrivalCurveValueLabel);
								inputValuesGrid.setWidget(3, 0, serviceCurveLabel);
								inputValuesGrid.setWidget(3, 1, serviceCurveValueLabel);
								inputValuesGrid.setWidget(4, 0, serviceCurveMaxLabel);
								inputValuesGrid.setWidget(4, 1, serviceCurveMaxValueLabel);
								
								
								inputValuesGrid.addStyleName("tableFormat");
															
								inputValuesGrid.getColumnFormatter().addStyleName(0, "resultFormatLabel");
								
								resultpmooValuesGrid.getColumnFormatter().addStyleName(0, "resultFormatLabel");
								resultsfaValuesGrid.getColumnFormatter().addStyleName(0, "resultFormatLabel");
								resulttfaValuesGrid.getColumnFormatter().addStyleName(0, "resultFormatLabel");
								
								resultpmooValuesGrid.addStyleName("tableFormat");
								resultsfaValuesGrid.addStyleName("tableFormat");
								resulttfaValuesGrid.addStyleName("tableFormat");
								verticalPanel.addStyleName("tableFormat");
								resultpmooValuesGrid.getColumnFormatter().addStyleName(1, "resultFormatValues");
								resultsfaValuesGrid.addStyleName("resultFormatValues");
								resulttfaValuesGrid.addStyleName("resultFormatValues");
								
								
								inputLabel.addStyleName("labelFormat");
								verticalPanel.add(inputLabel);
								verticalPanel.add(inputValuesGrid);
														
								resultLabel.addStyleName("labelFormat");
								verticalPanel.add(resultLabel);
								
								TFAresultsLabel.addStyleName("resultsTab");
								verticalPanel.add(TFAresultsLabel);
								verticalPanel.add(resulttfaValuesGrid);
								
								PMOOresultsLabel.addStyleName("resultsTab");
								verticalPanel.add(PMOOresultsLabel);
								verticalPanel.add(resultpmooValuesGrid);
								
								SFAresultsLabel.addStyleName("resultsTab");
								verticalPanel.add(SFAresultsLabel);
								verticalPanel.add(resultsfaValuesGrid);
								if(RPCflag==1){
									requestSfaAnalysis(network, configuration, flow_of_interest);
								}
															
								RootPanel.get().add(verticalPanel);
													
							}
							catch (Exception e) 
							{
								
								e.printStackTrace();
							}

							
				cnt=0;
			}
			
		}catch(Exception e){
			System.out.print(e);
		}
				
		}
				
	}

	
	private void requestSfaAnalysis(Network network,
			Configuration configuration, Flow flow_of_interest) {
		ncaawa.sfaAnalysis(
				network,
				configuration,
				flow_of_interest,
				new AsyncCallback<SeparateFlowAnalysisResults>() {
					public void onFailure(Throwable caught) {
						delayBoundLabel.setText( "RPC error" );
						backlogBoundLabel.setText( "RPC error" );
						System.out.println("RPC Error");
					}

					public void onSuccess(SeparateFlowAnalysisResults result) {
						//For sample demos
						if(choice==false){
							Element sfa_delay_bound=Document.get().getElementById("sfa_delay");
							sfa_delay_bound.setInnerHTML(result.delay_bound.toString());
							Element sfa_backlog_bound=Document.get().getElementById("sfa_backlog");
							sfa_backlog_bound.setInnerHTML(result.backlog_bound.toString());	
						}
					
						//For user defined network
						else if(choice==true)
						{
							e2eSFASCValueLabel.setText(result.betas_e2e.toString());
							perServerSFAValueLabel.setText(result.map__server__betas_lo.toString());
							delayBoundsfaLabel.setText( result.delay_bound.toString() );
							backlogBoundsfaLabel.setText( result.backlog_bound.toString() );
							resultsfaValuesGrid.setWidget(0,0, e2eSFASCLabel);
							resultsfaValuesGrid.setWidget(0, 1, e2eSFASCValueLabel);
							resultsfaValuesGrid.setWidget(1, 0, perServerSFALabel);
							resultsfaValuesGrid.setWidget(1, 1, perServerSFAValueLabel);
							resultsfaValuesGrid.setWidget(2, 0, delaysfaLabel);
							resultsfaValuesGrid.setWidget(2, 1, delayBoundsfaLabel);
							resultsfaValuesGrid.setWidget(3, 0, backlogsfaLabel);
							resultsfaValuesGrid.setWidget(3, 1, backlogBoundsfaLabel);	
						}
											
					}
				});
		
	}

	private void requestPmooAnalysis(Network network,
			Configuration configuration, Flow flow_of_interest) {
		ncaawa.pmooAnalysis(
				network,
				configuration,
				flow_of_interest,
				new AsyncCallback<PmooAnalysisResults>() {
					
					public void onFailure(Throwable caught) {
						delayBoundLabel.setText( "RPC error" );
						backlogBoundLabel.setText( "RPC error" );
						System.out.println("RPC Error");
					}

					public void onSuccess(PmooAnalysisResults result) {
		
						//For sample demos
						if(choice==false)
						{
						Element pmoo_delay_bound=Document.get().getElementById("pmoo_delay");
						pmoo_delay_bound.setInnerHTML(result.delay_bound.toString());

						Element pmoo_backlog_bound=Document.get().getElementById("pmoo_backlog");
						pmoo_backlog_bound.setInnerHTML(result.backlog_bound.toString());
						}
						//For user network
						else if(choice==true){
							e2ePMOOSFASCValueLabel.setText(result.betas_e2e.toString());
							delayBoundLabel.setText( result.delay_bound.toString() );
							backlogBoundLabel.setText( result.backlog_bound.toString() );
							resultpmooValuesGrid.setWidget(0, 0,e2ePMOOSFASCLabel);
							resultpmooValuesGrid.setWidget(0, 1, e2ePMOOSFASCValueLabel);
							resultpmooValuesGrid.setWidget(1, 0, delayLabel);
							resultpmooValuesGrid.setWidget(1, 1, delayBoundLabel);
							resultpmooValuesGrid.setWidget(2, 0, backlogLabel);
							resultpmooValuesGrid.setWidget(2, 1, backlogBoundLabel);	
						}
						
					}
				}); 

	}
	
	private void requestTfaAnalysis(Network network,
			Configuration configuration, Flow flow_of_interest) {
		ncaawa.tfaAnalysis(
				network,
				configuration,
				flow_of_interest,
				new AsyncCallback<TotalFlowAnalysisResults>() {
					public void onFailure(Throwable caught) {
						delayBoundLabel.setText( "RPC error" );
						backlogBoundLabel.setText( "RPC error" );
						System.out.println("RPC Error");
					}

					public void onSuccess(TotalFlowAnalysisResults result) {
						RPCflag=1;
						//For sample demos
						if(choice==false){
							Element tfa_delay_bound=Document.get().getElementById("tfa_delay");
							tfa_delay_bound.setInnerHTML(result.delay_bound.toString());
							Element tfa_backlog_bound=Document.get().getElementById("tfa_backlog");
							tfa_backlog_bound.setInnerHTML(result.backlog_bound.toString());	
						}
											
						//For user defined network
						else if(choice==true){
							perServerTFAValueLabel.setText(result.map__server__alphas.toString());
							delayBoundLabel.setText( result.delay_bound.toString() );
							backlogBoundLabel.setText( result.backlog_bound.toString() );
							resulttfaValuesGrid.setWidget(0, 0, delayLabel);
							resulttfaValuesGrid.setWidget(0, 1, delayBoundLabel);
							resulttfaValuesGrid.setWidget(1, 0, backlogLabel);
							resulttfaValuesGrid.setWidget(1, 1, backlogBoundLabel);
							resulttfaValuesGrid.setWidget(2, 0, perServerTFALabel);
							resulttfaValuesGrid.setWidget(2,1, perServerTFAValueLabel);
	
						}
					}
					 
				});

	}
	
}
