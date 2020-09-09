package utils;

import entities.Line;
import entities.Rectangle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static utils.Constants.*;
import static utils.Constants.FRAME_WIDTH;

public class RectangleService {
    private static Object lock = new Object();

    private static List<Rectangle> rectangles;

    private static List<Rectangle> sortedRectangles;
    
    private static List<Line> lines;

    private static BlockingQueue<Line> linesDeque = new LinkedBlockingDeque<>();

    private static int currentLineHeight;

    private static ExecutorService rectangleService;

    public static List<Rectangle> runRectanglePlacement(int threadNr, int rectangleNr) {
        rectangleService = Executors.newFixedThreadPool(threadNr);
        generateRectangles(rectangleNr);
        splitWork(threadNr);
        placeRectangles();

        while(rectangles.size() > 0){
            for(int i=0; i < rectangles.size(); i++){
                for(int j = 0; j < lines.size(); j++){
                    if(placeRectangle(rectangles.get(i), lines.get(j))){
                        break;
                    }else{
                        if(j == lines.size() - 1){
                            int lineIndex = lines.get(j).getIndex() + 1;
                            Rectangle[] remainingRectangles = new Rectangle[rectangles.size()];
                            createNewLine(rectangles.toArray(remainingRectangles), lineIndex);
                            break;
                        }
                    }
                }
            }
        }

        return sortedRectangles;
    }

    private static void placeRectangles() {
        sortedRectangles = new ArrayList<>();

        while(!linesDeque.isEmpty()){
            Future<String> futureMessage = rectangleService.submit(() -> {
                try {
                    processLine(linesDeque.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return "Thread " + Thread.currentThread().getId() + " sorted the rectangles";
            });

            try {
                futureMessage.get();
                System.out.println( futureMessage.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        awaitTerminationAfterShutdown(rectangleService);
    }

    private static void splitWork(int threadNumber) {
        int splitRectanglesSize = rectangles.size() / threadNumber;
        Rectangle[] splitRectangles = new Rectangle[splitRectanglesSize];
        int offset = 0;
        int lineIndex = 0;
        currentLineHeight = 0;
        lines = new ArrayList<>();

        for(int i = 0; i < rectangles.size(); i++){
            if(i!=0 && i % splitRectanglesSize == 0){
                linesDeque.add(createNewLine(splitRectangles, lineIndex));
                splitRectangles = new Rectangle[splitRectanglesSize];
                offset += splitRectanglesSize;
                lineIndex++;
            }

            splitRectangles[i - offset] = rectangles.get(i);

            if(i == rectangles.size() - 1){
                linesDeque.add(createNewLine(splitRectangles, lineIndex));
            }
        }
    }

    private static Line createNewLine(Rectangle[] splitRectangles, int lineIndex) {
        Line line = new Line();

        line.setHeight(currentLineHeight);
        currentLineHeight += splitRectangles[0].getHeight();
        line.setIndex(lineIndex);
        line.setRectangles(splitRectangles);
        lines.add(line);

        return line;
    }

    private static void generateRectangles(int nrOfRectangles){
        rectangles = new ArrayList<>();

        for(int i=1; i<= nrOfRectangles; i++){
            rectangles.add(new Rectangle( i,
                    (int) (MIN_RECTANGLE_WIDTH + (Math.random() * (MAX_RECTANGLE_WIDTH - MIN_RECTANGLE_WIDTH))),
                    (int) (MIN_RECTANGLE_HEIGHT + (Math.random() * (MAX_RECTANGLE_HEIGHT - MIN_RECTANGLE_HEIGHT)))
            ));
        }

        Collections.sort(rectangles, new HeightComparator());
    }

    private static void processLine(Line line){
        Rectangle[] retrievedRectangles = line.getRectangles();

        if(retrievedRectangles != null){
            for(int i=0; i < retrievedRectangles.length; i++){
                if(retrievedRectangles[i] != null){
                    placeRectangle(retrievedRectangles[i], line);
                }
            }

        }
    }

    private static boolean placeRectangle(Rectangle rectangle, Line line){
        if(rectangle.getWidth() < line.getRemainingWidth() ){
            rectangle.setY(line.getHeight());
            rectangle.setX(FRAME_WIDTH - line.getRemainingWidth());

            line.setRemainingWidth( line.getRemainingWidth() - rectangle.getWidth() );

            sortedRectangles.add(rectangle);
            return rectangles.remove(rectangle);
        }

        return false;
    }

    public static void awaitTerminationAfterShutdown(ExecutorService threadPool) {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
