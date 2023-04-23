import com.itextpdf.text.*;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatHistoryReport {

    // Title properties
    private static String TITLE = "Conversation History";
    private static Font TITLE_FONT = new Font(Font.FontFamily.TIMES_ROMAN, 26, Font.BOLD);
    private static final float TITLE_SECTION_HEIGHT = 65;

    private static String IMAGE_PATH = "/home/user/Desktop/img.png";
    private static float IMAGE_SCALE = 50;

    // Message properties
    private static Font MESSAGE_FONT = new Font(Font.FontFamily.TIMES_ROMAN, 12);
    private static BaseColor MESSAGE_COLOR = new BaseColor(0, 0, 0, 200);

    // Left properties
    private static float LEFT_ALIGN_RIGHT_BOUND = 480;
    private static float LEFT_ALIGN_LEFT_BOUND = 20;
    private static BaseColor LEFT_BOX_FILL_COLOR = new BaseColor(228, 255, 250, 60);
    private static BaseColor LEFT_BOX_BORDER_COLOR = new BaseColor(176, 225, 210);

    // Right properties
    private static float RIGHT_ALIGN_RIGHT_BOUND = 575;
    private static float RIGHT_ALIGN_LEFT_BOUND = 115;
    private static BaseColor RIGHT_BOX_FILL_COLOR = new BaseColor(255, 221, 221, 60);
    private static BaseColor RIGHT_BOX_BORDER_COLOR = new BaseColor(241, 208, 208);

    private static float BOX_MAX_WIDTH = RIGHT_ALIGN_RIGHT_BOUND - RIGHT_ALIGN_LEFT_BOUND;
    private static float PADDING = 21;
    private static float TOP_BOUND = 780;
    private static float BOTTOM_BOUND = 60;

    public static File generatePdfReport(List<Message> messages, String filePath) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();
        addTitle(document);
        addContent(document, writer, messages != null ? messages : new ArrayList<>());
        document.close();
        return new File(filePath);
    }

    private static void addTitle(Document doc) throws DocumentException {
        Paragraph p = new Paragraph();
        p.setFont(TITLE_FONT);
        Image image;
        try {
            image = Image.getInstance(IMAGE_PATH);
            image.scaleToFit(IMAGE_SCALE, IMAGE_SCALE);
            p.add(new Chunk(image, 0, 0, true));
        } catch (IOException e) {
            // Image not available.
        }
        p.add(TITLE);
        p.setAlignment(Element.ALIGN_CENTER);
        doc.add(p);
    }

    private static void addContent(Document document, PdfWriter writer, List<Message> messages) throws DocumentException {
        PdfContentByte canvas = writer.getDirectContent();
        float yPosition = TOP_BOUND - TITLE_SECTION_HEIGHT;
        
        for(Message message : messages) {
            if(yPosition <= BOTTOM_BOUND) {
                document.newPage();
                yPosition = TOP_BOUND;
            }
            Paragraph p = new Paragraph(message.getText(), MESSAGE_FONT);
            p.setIndentationLeft(10);
            p.setIndentationRight(10);
            float lastY = yPosition;

            float textLeftBound, textRightBound;
            float width = Math.min(BOX_MAX_WIDTH, ColumnText.getWidth(new Phrase(message.getText(), MESSAGE_FONT)) + PADDING);
            if(message.isRightAlign()) {
                textLeftBound = (width == BOX_MAX_WIDTH) ? RIGHT_ALIGN_LEFT_BOUND : RIGHT_ALIGN_RIGHT_BOUND - width;
                textRightBound = RIGHT_ALIGN_RIGHT_BOUND;
            } else {
                textRightBound = (width == BOX_MAX_WIDTH) ? LEFT_ALIGN_RIGHT_BOUND : LEFT_ALIGN_LEFT_BOUND + width;
                textLeftBound = LEFT_ALIGN_LEFT_BOUND;
            }

            Rectangle rectangle = new Rectangle(textLeftBound, BOTTOM_BOUND, textRightBound, yPosition);
            ColumnText ct = new ColumnText(canvas);
            ct.setSimpleColumn(rectangle);
            ct.addElement(p);
            canvas.setColorFill(MESSAGE_COLOR);

            float before = ct.getFilledWidth();
            int status = ct.go();
            float after = ct.getFilledWidth();
            if(after != before) {
                addBox(ct, canvas, message, width, lastY);
                yPosition = ct.getYLine() - PADDING;
            }

            while (ColumnText.hasMoreText(status)) {
                document.newPage();
                ct.setSimpleColumn(new Rectangle(textLeftBound, BOTTOM_BOUND, textRightBound, TOP_BOUND));
                status = ct.go();
                addBox(ct, canvas, message, width, TOP_BOUND);
                yPosition = ct.getYLine() - PADDING;
            }
        }
    }

    private static void addBox(ColumnText ct, PdfContentByte canvas, Message message, float width, float yPosition) {
        float pos = ct.getYLine() - 10;
        float boxLeftBound;
        if(message.isRightAlign()) {
            boxLeftBound = (width == BOX_MAX_WIDTH) ? RIGHT_ALIGN_LEFT_BOUND : RIGHT_ALIGN_RIGHT_BOUND - width;
            canvas.setColorFill(RIGHT_BOX_FILL_COLOR);
            canvas.setColorStroke(RIGHT_BOX_BORDER_COLOR);
        } else {
            boxLeftBound = LEFT_ALIGN_LEFT_BOUND;
            canvas.setColorFill(LEFT_BOX_FILL_COLOR);
            canvas.setColorStroke(LEFT_BOX_BORDER_COLOR);
        }
        canvas.roundRectangle(boxLeftBound, pos, width, yPosition - pos, 8);
        canvas.fillStroke();
    }

}