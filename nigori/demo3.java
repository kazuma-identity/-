import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

// --- Figure クラスとその派生クラス ---
class Figure {
    protected int x, y, width, height;
    protected Color color;
    protected int HP;

    public Figure(int x, int y, int w, int h, Color c) {
        this.x = x;
        this.y = y;
        width = w;
        height = h;
        color = c;
        HP = 0;
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setHP(int HP) {
        this.HP = HP;
    }

    public int getHP() {
        return HP;
    }

    public void reduceHP(int damage) {
        HP -= damage;
    }

    public void reshape(int x1, int y1, int x2, int y2) {
        int newx = Math.min(x1, x2);
        int newy = Math.min(y1, y2);
        int neww = Math.abs(x1 - x2);
        int newh = Math.abs(y1 - y2);
        setLocation(newx, newy);
        setSize(neww, newh);
    }

    public void draw(Graphics g) {}
}

class CircleFigure extends Figure {
    public CircleFigure(int x, int y, int diameter, Color c) {
        super(x, y, diameter, diameter, c);
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillOval(x, y, width, height); // 円を描画
    }
}

class RectangleFigure extends Figure {
    public RectangleFigure(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.drawRect(x, y, width, height);
    }
}

class FillRectFigure extends Figure {
    public FillRectFigure(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillRect(x, y, width, height);
    }
}

// --- Model ---
class DrawModel extends Observable {
    protected ArrayList<Figure> fig;
    protected Figure drawingFigure;
    protected Color currentColor;
    protected boolean Castle;
    protected Figure lastCircle;
    protected String chosenFigure;

    public DrawModel() {
        fig = new ArrayList<>();
        drawingFigure = null;
        currentColor = Color.black;
        Castle = false;
    }

    public int getCastleHP() {
        if(!fig.isEmpty()) {
            return fig.get(0).getHP();
        }
        return 0;
    }

    public void setCurrentColor(Color color) {
        currentColor = color;
        setChanged();
        notifyObservers();
    }

    public ArrayList<Figure> getFigures() {
        return fig;
    }

    public void setCastle(boolean castle) {
        Castle = castle;
        setChanged();
        notifyObservers();
    }

    public boolean isCastle() {
        return Castle;
    }

    public void createFigure(int x, int y) {
        Figure f;
        if (!Castle) {
            f = new FillRectFigure(x, y, 0, 0, currentColor);
            f.setHP(5);
        } else if (chosenFigure == "RED") {
            f = new RectangleFigure(x, y, 0, 0, Color.red);
        } else if (chosenFigure == "BLUE") {
            f = new RectangleFigure(x, y, 0, 0, Color.blue);
        } else {
            f = new RectangleFigure(x, y, 0, 0, Color.green);
        }
        fig.add(f);
        drawingFigure = f;
        setChanged();
        notifyObservers();
    }

    public void removeFigure(Figure figure) {
        if (fig.remove(figure)) { // リストから削除
            setChanged(); // モデルが変更されたことを通知
            notifyObservers(); // ビューを更新
        }
    }

    // すべての図形を削除するメソッド
    public void clearFigures() {
        fig.clear(); // リストをクリア
        setChanged(); // モデルが変更されたことを通知
        notifyObservers(); // ビューを更新
    }

    public void reshapeFigure(int x1, int y1, int x2, int y2) {
        if (drawingFigure != null) {
            drawingFigure.reshape(x1, y1, x2, y2);
            setChanged();
            notifyObservers();
        }
    }

    public void setDrawFigure(String f) {
        chosenFigure = f;
    }

    public boolean checkCollisionAndRemoveC(Figure circle) {
        Iterator<Figure> iterator = fig.iterator();
        boolean collisionDetected = false;
        while (iterator.hasNext()) {
            Figure f = iterator.next();

            // 円と他の図形が接触しているかを判定
            if (isTouching(circle, f)) {
                if (f == fig.get(0)) { // キャッスルに衝突した場合
                    f.reduceHP(1); // キャッスルの HP を減らす
                    if (f.getHP() <= 0) { // HP が 0 になったらゲームオーバー
                        setChanged();
                        notifyObservers("GameOver");
                    }
                } else {
                    iterator.remove(); // 他の図形を削除
                }
                collisionDetected = true;
                break;
            }
        }
        if (collisionDetected) {
            removeFigure(circle);
        }
        return collisionDetected; 
    }

    // 円と図形の接触判定
    private boolean isTouching(Figure circle, Figure other) {
        int circleCenterX = circle.x + circle.width / 2;
        int circleCenterY = circle.y + circle.height / 2;
        int radius = circle.width / 2;

        // 他の図形の境界を計算
        int otherLeft = other.x;
        int otherRight = other.x + other.width;
        int otherTop = other.y;
        int otherBottom = other.y + other.height;

        // 円の中心が他の図形の境界内にあるかチェック
        boolean isTouchingX = circleCenterX + radius > otherLeft && circleCenterX - radius < otherRight;
        boolean isTouchingY = circleCenterY + radius > otherTop && circleCenterY - radius < otherBottom;

        return isTouchingX && isTouchingY;
    }

    public void reset() {
        fig.clear(); // すべての図形を削除
        drawingFigure = null; // 現在描画中の図形をリセット
        currentColor = Color.black; // 色をリセット
        Castle = false; // フラグをリセット
        lastCircle = null; // 最後に描画した円をリセット
        chosenFigure = null; // 選択した図形タイプをリセット

        setChanged();
        notifyObservers(); // モデルの変更を通知してビューを更新
    }

    public void addMovingCircle() {
        javax.swing.Timer timer = new javax.swing.Timer(250, new ActionListener() { // 500ミリ秒ごとに実行
            private Random rand = new Random();
            private int circleX = 480; // 右側の初期位置
            private int circleY = rand.nextInt(480); // 中心位置（ランダム初期化）
            private final int diameter = 20; // 円の直径
            private final int step = 40; // 移動距離

            @Override
            public void actionPerformed(ActionEvent e) {
                if (lastCircle != null) {
                    removeFigure(lastCircle);
                }

                // 新しい円を生成して左に移動させる
                circleX -= step;
                CircleFigure circle = new CircleFigure(circleX, circleY - diameter / 2, diameter, Color.orange);

                if (circleX + diameter < 0) {
                    lastCircle = null;
                    circleX = 480;
                    circleY = rand.nextInt(480); // 円が画面外に到達したらY位置をランダムに変更
                }

                // 接触判定（キャッスルを含む全図形との衝突判定）
                if (checkCollisionAndRemoveC(circle)) {
                    // 衝突してもゲームが継続する場合はリセットせず、次の円を生成
                    if (getCastleHP() > 0) {
                        circleX = 480; // 新しい円の初期位置を設定
                        circleY = rand.nextInt(480); // 衝突時もY位置をランダムに変更
                    } else {
                        ((javax.swing.Timer) e.getSource()).stop(); // HPが0でタイマーを停止
                    }
                } else {
                    fig.add(circle); // 新しい円を図形リストに追加
                    lastCircle = circle;
                }

                // 更新を通知
                setChanged();
                notifyObservers();
            }
        });
        timer.start();
    }

}

// --- View ---
class ViewPanel extends JPanel implements Observer {
    protected DrawModel model;
    protected JButton choose1, choose2, choose3;
    protected JButton gameOver;
    protected boolean enableDrawing = false;

    public ViewPanel(DrawModel m, DrawController c) {
        this.setBackground(Color.white);
        this.addMouseListener(c);
        this.addMouseMotionListener(c);
        this.addKeyListener(c);
        this.setFocusable(true);
        model = m;
        model.addObserver(this);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Figure f : model.getFigures()) {
            f.draw(g);
        }
        // キャッスルの HP を表示
        int castleHP = model.getCastleHP();
        g.setColor(Color.black);
        g.drawString("Castle HP: " + castleHP, 10, 20);
    }

    public void reset() {
        // すべてのコンポーネントを削除してリセット
        this.removeAll();

        // ボタンの参照をリセット
        choose1 = null;
        choose2 = null;
        choose3 = null;
        gameOver = null;

        // 再描画
        enableDrawing = false;
        this.revalidate();
        this.repaint();
    }

    public void update(Observable o, Object arg) {
        repaint();

        // 円が画面外に到達した際にボタンを生成
        if (gameOver == null && "GameOver".equals(arg)) {
            gameOver = new JButton("Game Over");
            gameOver.addActionListener(e -> {
                // スクリーン3に移動
                ((CardLayout) this.getParent().getLayout()).show(this.getParent(), "Screen3");
            });
            this.add(gameOver);
            this.revalidate();
            this.repaint();
        }
        if (model.isCastle() && choose1 == null) {
            choose1 = new JButton("RED");
            choose2 = new JButton("BLUE");
            choose3 = new JButton("GREEN");
            choose1.addActionListener(e -> {
                enableDrawing = true; // 図形を描画するモードを有効化
                model.setDrawFigure("RED");
                choose1.setEnabled(false); // ボタンを無効化
                choose2.setEnabled(false);
                choose3.setEnabled(false);
                model.addMovingCircle(); // アニメーションを開始
            });
            choose2.addActionListener(e -> {
                enableDrawing = true; // 図形を描画するモードを有効化
                model.setDrawFigure("BLUE");
                choose1.setEnabled(false); // ボタンを無効化
                choose2.setEnabled(false);
                choose3.setEnabled(false);
                model.addMovingCircle(); // アニメーションを開始
            });
            choose3.addActionListener(e -> {
                enableDrawing = true; // 図形を描画するモードを有効化
                model.setDrawFigure("GREEN");
                choose1.setEnabled(false); // ボタンを無効化
                choose2.setEnabled(false);
                choose3.setEnabled(false);
                model.addMovingCircle(); // アニメーションを開始
            });
            this.add(choose1);
            this.add(choose2);
            this.add(choose3);
            this.revalidate();
            this.repaint();
        }                
    }

    public boolean isDrawingEnabled() {
        return enableDrawing;
    }
}

// --- Controller ---
class DrawController implements MouseListener, MouseMotionListener, KeyListener {
    protected DrawModel model;
    protected ViewPanel view;
    protected int pushStartX, pushStartY;

    public DrawController(DrawModel a, ViewPanel b) {
        model = a;
    }

    public void keyPressed(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}

    public void mouseClicked(MouseEvent e) {
        if (view.isDrawingEnabled() || !model.isCastle()) {
            pushStartX = e.getX();
            pushStartY = e.getY();
            model.createFigure(pushStartX, pushStartY);
            model.reshapeFigure(pushStartX, pushStartY, pushStartX + 50, pushStartY + 50);
            model.setCastle(true);
        }
    }

    public void mousePressed(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
}

// --- Main Frame ---
class DrawFrame extends JFrame {
    CardLayout cardLayout;
    JPanel mainPanel;

    public DrawFrame() {
        // 全体のレイアウト
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // モデルの作成
        DrawModel model = new DrawModel();

        // コントローラーを先に作成
        DrawController cont = new DrawController(model, null); // 一時的に null を渡す

        // スクリーン① (タイトル画面)
        JPanel screen1 = new JPanel();
        screen1.setLayout(new BorderLayout());
        JLabel title = new JLabel("Tower Defence", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        JButton startButton = new JButton("START");
        startButton.addActionListener(e -> cardLayout.show(mainPanel, "Screen2"));

        screen1.add(title, BorderLayout.CENTER);
        screen1.add(startButton, BorderLayout.SOUTH);

        // スクリーン② (描画画面)
        ViewPanel screen2 = new ViewPanel(model, cont); // コントローラーを渡す

        // コントローラーがビューを参照するように設定
        cont.view = screen2;

        // スクリーン③（終了画面）
        JPanel screen3 = new JPanel();
        JLabel gameoverLabel = new JLabel("Game Over", SwingConstants.CENTER);
        gameoverLabel.setFont(new Font("Arial", Font.BOLD, 24));
        JButton retryButton = new JButton("RETRY");
        retryButton.addActionListener(e -> {
            model.reset();
            screen2.reset();
            cardLayout.show(mainPanel, "Screen1");
        });

        screen3.setLayout(new BorderLayout());
        screen3.add(gameoverLabel, BorderLayout.CENTER);
        screen3.add(retryButton, BorderLayout.SOUTH);        

        // スクリーンをカードとして追加
        mainPanel.add(screen1, "Screen1");
        mainPanel.add(screen2, "Screen2");
        mainPanel.add(screen3, "Screen3");

        // 初期設定
        this.add(mainPanel);
        this.setTitle("Tower Defence");
        this.setSize(500, 500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        new DrawFrame();
    }
}
