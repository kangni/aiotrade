package org.aiotrade.platform.test

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.lang.ref.Reference
import java.lang.ref.WeakReference
import java.util.Locale
import java.util.ResourceBundle
import javax.swing.BorderFactory
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JSplitPane
import javax.swing.JTabbedPane
import javax.swing.SwingConstants
import javax.swing.UIManager
import org.aiotrade.lib.charting.chart.QuoteChart
import org.aiotrade.lib.charting.laf.CityLights
import org.aiotrade.lib.charting.laf.LookFeel
import org.aiotrade.lib.charting.view.ChartingController
import org.aiotrade.lib.charting.view.ChartingControllerFactory
import org.aiotrade.lib.math.timeseries.TFreq
import org.aiotrade.lib.math.timeseries.computable.IndicatorDescriptor
import org.aiotrade.lib.math.timeseries.descriptor.AnalysisContents
import org.aiotrade.lib.util.swing.plaf.AIOTabbedPaneUI
import org.aiotrade.lib.chartview.AnalysisChartViewContainer
import org.aiotrade.lib.chartview.AnalysisQuoteChartView
import org.aiotrade.lib.chartview.RealTimeBoardPanel
import org.aiotrade.lib.chartview.RealTimeChartViewContainer
import org.aiotrade.lib.indicator.VOLIndicator
import org.aiotrade.lib.securities.dataserver.QuoteContract
import org.aiotrade.lib.securities.dataserver.TickerContract
import org.aiotrade.lib.securities.dataserver.TickerServer
import org.aiotrade.lib.securities.QuoteSer
import org.aiotrade.lib.securities.Sec
import org.aiotrade.lib.securities.Stock
import org.aiotrade.lib.dataserver.yahoo.YahooQuoteServer
import org.aiotrade.lib.indicator.basic.MAIndicator
import org.aiotrade.lib.indicator.basic.RSIIndicator
import scala.collection.mutable.HashSet


/**
 *
 * @author Caoyuan Deng
 */
object Util {
  val EAST_REGIONS= Array(Locale.CHINA, Locale.TAIWAN, Locale.JAPAN, Locale.KOREA)

  val BUNDLE = ResourceBundle.getBundle("org.aiotrade.platform.test.Bundle")
  val viewContainers = new HashSet[Reference[AnalysisChartViewContainer]]
  // 544 width is the proper size to fit 2 pixes 241 bar (one trading day's 1min)
  private val MAIN_PANE_WIDTH = 544
}

class Util {
  import Util._

  private var tickerServer: TickerServer = _
  private var sec: Sec = _

  /***
   * @param para parameters defined
   * @param data in previsous format. current only dailly supported.
   * @return a image
   */
  def init(
    pane: Container,
    width: Int, height: Int,
    symbol: String,
    category: String,
    sname: String,
    quoteServer: Class[_],
    tickerServer: Class[_]
  ): Collection[Reference[AnalysisChartViewContainer]] = {

    val leftPaneWidth = 240
    
    val mainWidth = width - leftPaneWidth

    val locale = Locale.getDefault

    val laf = new CityLights
    laf.setAntiAlias(true)
    EAST_REGIONS find (_.getCountry.equals(locale.getCountry)) foreach {x =>
      laf.setPositiveNegativeColorReversed(true)
    }
    LookFeel() = laf

    setUIStyle

    val freqOneMin = TFreq.ONE_MIN
    val freqDaily = TFreq.DAILY

    var quoteContracts: List[QuoteContract] = Nil
    val dailyQuoteContract = createQuoteContract(symbol, category, sname, freqDaily, false, quoteServer)
    quoteContracts ::= dailyQuoteContract
    val supportOneMin = dailyQuoteContract.isFreqSupported(freqOneMin)
    val oneMinQuoteContract = createQuoteContract(symbol, category, sname, freqOneMin, supportOneMin, quoteServer)
    quoteContracts ::= oneMinQuoteContract
    val tickerContract =
      if (tickerServer != null) {
        createTickerContract(symbol, category, sname, freqOneMin, tickerServer)
      } else null

    sec = new Stock(symbol, quoteContracts, tickerContract)
    val market =
      if (quoteServer.getName == classOf[YahooQuoteServer].getName) {
        YahooQuoteServer.marketOf(symbol)
      } else {
        null//ApcQuoteServer.GetMarket(symbol)
      }
    sec.market = market

    val dailyContents = createAnalysisContents(symbol, freqDaily, quoteServer, tickerServer);
    dailyContents.addDescriptor(dailyQuoteContract)
    dailyContents.serProvider = sec
    loadSer(dailyContents)

    val rtContents = createAnalysisContents(symbol, freqOneMin, quoteServer, tickerServer);
    rtContents.addDescriptor(oneMinQuoteContract)
    rtContents.serProvider = sec
    loadSer(rtContents)

    // --- other freqs:
//    val oneMinViewContainer = createViewContainer(
//      sec.serOf(freqOneMin).getOrElse(null),
//      rtContents,
//      symbol,
//      QuoteChart.Type.Line,
//      pane)
//
//    oneMinViewContainer.setPreferredSize(new Dimension(mainWidth, height))
//    viewContainers.add(new WeakReference[AnalysisChartViewContainer](oneMinViewContainer))

    val dailyViewContainer = createViewContainer(
      sec.serOf(freqDaily).getOrElse(null),
      dailyContents,
      symbol,
      QuoteChart.Type.Candle,
      pane)
    //dailyViewContainer.setPreferredSize(new Dimension(mainWidth, height))
    viewContainers.add(new WeakReference[AnalysisChartViewContainer](dailyViewContainer))

    //val rtViewContainer = createRealTimeViewContainer(sec, rtContents, pane)

    try {
      pane.setLayout(new BorderLayout)
      pane.setLayout(new BorderLayout)


//      val tabbedPane = createTabbedPane
//      tabbedPane.setFocusable(false)

      //tabbedPane.setBorder(new AIOScrollPaneStyleBorder(LookFeel()().borderColor));

      //splitPane.add(JSplitPane.LEFT, tabbedPane)

//      val rtPanel = new JPanel(new BorderLayout)
//      rtPanel.add(BorderLayout.CENTER, rtViewContainer)
//
//      val oneMinPanel = new JPanel(new BorderLayout)
//      oneMinPanel.add(BorderLayout.CENTER, oneMinViewContainer)
//
//      tabbedPane.addTab(BUNDLE.getString("daily"), dailyPanel)
//      tabbedPane.addTab(BUNDLE.getString("realTime"), rtPanel)
//      tabbedPane.addTab(BUNDLE.getString("oneMin"), oneMinPanel)
//      
//      val rtBoardBoxV = Box.createVerticalBox
//      rtBoardBoxV.add(Box.createVerticalStrut(22)) // use to align top of left pane's content pane
//      rtBoardBoxV.add(rtBoard)
//
//      val rtBoardBoxH = Box.createHorizontalBox
//      rtBoardBoxH.add(Box.createHorizontalStrut(5))
//      rtBoardBoxH.add(rtBoardBoxV)

      val dailyPanel = new JPanel(new BorderLayout)
      dailyPanel.add(BorderLayout.CENTER, dailyViewContainer)

      val rtBoard = new RealTimeBoardPanel(sec, rtContents)
      rtBoard.setPreferredSize(new Dimension(leftPaneWidth, height))


      val splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
      splitPane.setFocusable(false)
      splitPane.setBackground(Color.WHITE)
      splitPane.setBorder(BorderFactory.createEmptyBorder)
      splitPane.setOneTouchExpandable(false)
      splitPane.setDividerSize(1)
      //splitPane.setDividerLocation(width - leftPaneWidth)
      //splitPane.setDividerLocation(0.7D)

      splitPane.add(JSplitPane.LEFT, dailyPanel)
      splitPane.add(JSplitPane.RIGHT, rtBoard)
      //pane.add(BorderLayout.NORTH, createToolBar(width));
      pane.add(BorderLayout.CENTER, splitPane)

      watchRealTime(rtContents, rtBoard)

      //container.getController().setCursorCrossLineVisible(showLastClose(apcpara));
    } catch {case ex: Exception => ex.printStackTrace}

    viewContainers
  }

//    private JToolBar createToolBar(int width) {
//        JToolBar toolBar = new JToolBar();
//        toolBar.add(new ZoomInAction());
//        toolBar.add(new ZoomOutAction());
//
//        toolBar.setPreferredSize(new Dimension(width, 18));
//        for (Component c : toolBar.getComponents()) {
//            c.setFocusable(false);
//        }
//
//        return toolBar;
//    }

  private def createQuoteContract(symbol: String, category: String, sname: String, freq: TFreq, refreshable: Boolean, server: Class[_]): QuoteContract = {
    val dataContract = new QuoteContract

    dataContract.active = true
    dataContract.serviceClassName = server.getName

    dataContract.symbol = symbol
    dataContract.category = category
    dataContract.shortName = sname
    dataContract.secType = Sec.Type.Stock
    dataContract.exchange= "SSH"
    dataContract.primaryExchange = "SSH"
    dataContract.currency = "USD"

    dataContract.dateFormatPattern = "yyyy-MM-dd"

    dataContract.freq = freq

    dataContract.refreshable = refreshable
    dataContract.refreshInterval = 5

    dataContract
  }

  private def createTickerContract(symbol: String, category: String, sname: String, freq: TFreq, server: Class[_]): TickerContract = {
    val dataContract = new TickerContract

    dataContract.active = true
    dataContract.serviceClassName = server.getName

    dataContract.symbol = symbol
    dataContract.category = category
    dataContract.shortName = sname
    dataContract.secType = Sec.Type.Stock
    dataContract.exchange = "SSH"
    dataContract.primaryExchange = "SSH"
    dataContract.currency = "USD"

    dataContract.dateFormatPattern = "yyyy-MM-dd-HH-mm-ss"
    dataContract.freq = freq
    dataContract.refreshable = true
    dataContract.refreshInterval = 5

    dataContract
  }

  private def createAnalysisContents(symbol: String, freq: TFreq, quoteServer: Class[_], tickerServer: Class[_]): AnalysisContents = {
    val contents = new AnalysisContents(symbol)

    contents.addDescriptor(createIndicatorDescriptor(classOf[MAIndicator], freq))
    contents.addDescriptor(createIndicatorDescriptor(classOf[VOLIndicator], freq))
    contents.addDescriptor(createIndicatorDescriptor(classOf[RSIIndicator], freq))

    contents
  }

//    private static final AnalysisContents createRealTimeContents(String symbol, Frequency freq, Class quoteServer) {
//        AnalysisContents contents = new AnalysisContents(symbol);
//
//        contents.addDescriptor(createIndicatorDescriptor(VOLIndicator.class, freq));
//
//        QuoteContract quoteContract = createQuoteContract(symbol, freq, quoteServer);
//        TickerContract tickerContract = createTickerContract(symbol, ApcTickerServer.class);
//        contents.addDescriptor(quoteContract);
//        contents.addDescriptor(tickerContract);
//
//        return contents;
//    }
  private def loadSer(contents: AnalysisContents) {
    val quoteContract = contents.lookupActiveDescriptor(classOf[QuoteContract]).get

    val freq = quoteContract.freq
    if (!quoteContract.isFreqSupported(freq)) {
      return
    }

    val sec = contents.serProvider
    var mayNeedsReload = false
    if (sec == null) {
      return
    } else {
      mayNeedsReload = true
    }

    if (mayNeedsReload) {
      sec.clearSer(freq)
    }

    if (!sec.isSerLoaded(freq) && !sec.isSerInLoading(freq)) {
      sec.loadSer(freq)
    }
		
  }

  private def createIndicatorDescriptor(clazz: Class[_], freq: TFreq): IndicatorDescriptor = {
    val indicator = new IndicatorDescriptor
    indicator.active = true
    indicator.serviceClassName = clazz.getName
    indicator.freq = freq
    indicator
  }

  private def createViewContainer(
    ser: QuoteSer,
    contents: AnalysisContents,
    atitle: String,
    tpe: QuoteChart.Type,
    parent: Component
  ): AnalysisChartViewContainer = {

    var title = atitle

    val controller = ChartingControllerFactory.createInstance(ser, contents)
    val viewContainer = controller.createChartViewContainer(classOf[AnalysisChartViewContainer], parent).get

    if (title == null) {
      title = ser.freq.name
    }
    title = " " + title + " "

    viewContainer.controller.isCursorCrossLineVisible = true
    viewContainer.controller.isOnCalendarMode = false
    val masterView = viewContainer.masterView.asInstanceOf[AnalysisQuoteChartView]
    masterView.switchQuoteChartType(tpe)
    masterView.xControlPane.setVisible(true)
    masterView.yControlPane.setVisible(true)

    /** inject popup menu from this TopComponent */
    //viewContainer.setComponentPopupMenu(popupMenuForViewContainer);
    viewContainer
  }

  private def createRealTimeViewContainer(sec: Sec, contents: AnalysisContents, parent: Component): RealTimeChartViewContainer = {
    var masterSer = sec.serOf(TFreq.ONE_MIN).getOrElse(sec.tickerSer)
    val controller = ChartingControllerFactory.createInstance(masterSer, contents)
    val viewContainer = controller.createChartViewContainer(classOf[RealTimeChartViewContainer], parent).get
    viewContainer
  }

  private def createTabbedPane: JTabbedPane = {
    val tabbedPane = new JTabbedPane(SwingConstants.TOP)
    tabbedPane.setFocusable(false)
    return tabbedPane
//        tabbedPane.addChangeListener(new ChangeListener() {
//            private Color selectedColor = new Color(177, 193, 209);
//
//            public void stateChanged(ChangeEvent e) {
//                JTabbedPane tp = (JTabbedPane)e.getSource();
//
//                for (int i = 0; i < tp.getTabCount(); i++) {
//                    tp.setBackgroundAt(i, null);
//                }
//                int idx = tp.getSelectedIndex();
//                tp.setBackgroundAt(idx, selectedColor);
//
//                //updateToolbar();
//
//                if (tp.getSelectedComponent() instanceof AnalysisChartViewContainer) {
//                    AnalysisChartViewContainer viewContainer = (AnalysisChartViewContainer)tp.getSelectedComponent();
//                    MasterSer masterSer = viewContainer.getController().getMasterSer();
//
//                    /** update the descriptorGourp node's children according to selected viewContainer's time frequency: */
//
//                    Node secNode = NetBeansPersistenceManager.getOccupantNode(contents);
//                    assert secNode != null : "There should be at least one created node bound with descriptors here, as view has been opened!";
//                    for (Node groupNode : secNode.getChildren().getNodes()) {
//                        ((GroupNode)groupNode).setTimeFrequency(masterSer.getFreq());
//                    }
//
//                    /** update the supportedFreqsComboBox */
//                    setSelectedFreqItem(masterSer.getFreq());
//                }
//            }
//        });
//
  }

  private def setUIStyle {
//        UIDefaults defs = UIManager.getDefaults();
//        Enumeration keys = defs.keys();
//        while (keys.hasMoreElements()) {
//            Object key = keys.nextElement();
//            if (key.toString().startsWith("TabbedPane")) {
//                System.out.println(key);
//            }
//        }

    UIManager.put("TabbedPaneUI", classOf[AIOTabbedPaneUI].getName)
    /** get rid of the ugly border of JTabbedPane: */
//        Insets oldInsets = UIManager.getInsets("TabbedPane.contentBorderInsets");
    /*- set top insets as 1 for TOP placement if you want:
     UIManager.put("TabbedPane.contentBorderInsets", new Insets(1, 0, 0, 0));
     */
    //UIManager.getColor("TabbedPane.tabAreaBackground");
    UIManager.put("TabbedPane.selected", LookFeel().backgroundColor)
    UIManager.put("TabbedPane.selectHighlight", LookFeel().backgroundColor)

    UIManager.put("TabbedPane.unselectedBackground", Color.WHITE)
    UIManager.put("TabbedPane.selectedBorderColor", LookFeel().borderColor)
//        UIManager.put("TabbedPane.contentBorderInsets", new Insets(2, 0, 0, 1));
//        UIManager.put("TabbedPane.contentBorderInsets", oldInsets);
//        UIManager.put("TabbedPane.font", new Font("Dialog", Font.PLAIN, 11));
//        UIManager.put("TabbedPane.foreground", LookFeel()().borderColor);
//        UIManager.put("TabbedPane.background", Color.WHITE);
//        UIManager.put("TabbedPane.shadow", Color.GRAY);
//        UIManager.put("TabbedPane.darkShadow", Color.GRAY);
  }

  def watchRealTime(contents: AnalysisContents, rtBoard: RealTimeBoardPanel) {
    sec.subscribeTickerServer

    tickerServer = sec.tickerServer
    if (tickerServer == null) {
      return;
    }

    tickerServer.tickerSnapshotOf(sec.tickerContract.symbol) foreach {tickerSnapshot =>
      tickerSnapshot.addObserver(rtBoard)
    }
  }

  @throws(classOf[Exception])
  def paintToImage(
    container: AnalysisChartViewContainer,
    controller: ChartingController,
    begTime: Long,
    endTime: Long,
    width: Int,
    height: Int,
    fm: JFrame
  ): BufferedImage = {

    container.setPreferredSize(new Dimension(width, height))

    container.setBounds(0, 0, width, height)

    fm.getContentPane.add(container)
    fm.pack

    val begPos = controller.masterSer.rowOfTime(begTime)
    val endPos = controller.masterSer.rowOfTime(endTime)
    val nBars = endPos - begPos + 1

    // wViewport should minus AxisYPane's width
    val wViewPort = width - container.masterView.axisYPane.getWidth
    controller.setWBarByNBars(wViewPort, nBars)


//        /** backup: */
    val backupRightCursorPos = controller.rightSideRow
    val backupReferCursorPos = controller.referCursorRow

    controller.setCursorByRow(backupReferCursorPos, endPos - 1, true)

    container.paintToImage

    container.paintToImage.asInstanceOf[BufferedImage]
    /** restore: */
    //controller.setCursorByRow(backupReferCursorPos, backupRightCursorPos);
  }

  def releaseAll {
    // Since ticker server is singleton, will be reused in browser, should unSubscribe it to get tickerSnapshot etc to be reset
    sec.unSubscribeTickerServer
  }
}