package com.bradmcevoy.web;

import com.bradmcevoy.web.ImageFile;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.utils.XmlUtils2;
import com.bradmcevoy.web.component.AbstractInput;
import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.component.DateInput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jdom.Element;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class ChartImage extends ImageFile {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ImageFile.class);
    private static final long serialVersionUID = 1L;
    
    private String title;
    private String xTitle;
    private String yTitle;
    private List<DataSeries> dataSeriess = new ArrayList<DataSeries>();
    private AbstractInput from = new DateInput(this, "from");
    private AbstractInput to = new DateInput(this, "to");

    public ChartImage(Folder parentFolder, String newName) {
        super("image", parentFolder, newName);
    }

    @Override
    public void populateXml(Element e2) {
        super.populateXml(e2);
        this.from = (AbstractInput) XmlUtils2.restoreObject(e2.getChild("from"),this);
        this.to = (AbstractInput) XmlUtils2.restoreObject(e2.getChild("to"),this);
    }

    
    
    @Override
    protected BaseResource newInstance(Folder parent, String newName) {
        return new ChartImage(parent, newName);
    }

    @Override
    public Element toXml(Addressable container, Element el) {
        Element elThis = super.toXml(container, el);
        Element elFrom = new Element("from");
        elThis.addContent(elFrom);
        from.toXml(this, elFrom);
        Element elTo = new Element("to");
        elThis.addContent(elTo);
        to.toXml(this, elTo);
        return elThis;
    }

    
    @Override
    public void setContent(InputStream in) {
        // do nothing
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Object fromVal = from.getValue();
        Object toVal = to.getValue();
        fill(dataset, fromVal, toVal);
        JFreeChart chart = ChartFactory.createBarChart(title,xTitle, yTitle, dataset, PlotOrientation.VERTICAL, false,true, false);
        try {
            ChartUtilities.writeChartAsPNG(out, chart, 600, 400);
            out.flush();
        } catch (IOException e) {
            System.err.println("Problem occurred creating chart.");
        }

    }

    protected void fill(DefaultCategoryDataset dataset, Object from, Object to) {
        for( DataSeries ds : dataSeriess ) {
            TupleList list = ds.getSeries(from, to);
            for( Tuple t : list ) {
                dataset.setValue(t.value, ds.getName(),t.key);
            }
            
        }
    }
}
