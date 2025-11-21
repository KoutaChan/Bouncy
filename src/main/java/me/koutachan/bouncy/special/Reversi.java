package me.koutachan.bouncy.special;


import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.utils.ItemCreator;
import me.koutachan.bouncy.utils.gui.GuiBase;
import me.koutachan.bouncy.utils.gui.GuiCancelType;
import me.koutachan.bouncy.utils.gui.button.impl.ItemButton;
import me.koutachan.bouncy.utils.gui.button.impl.LambdaButton;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Reversi extends GuiBase<Reversi> {
    private static final int BOARD_ROWS = 6;
    private static final int BOARD_COLS = 8;
    private static final int EMPTY = 0;
    private static final int BLACK = 1;
    private static final int WHITE = 2;

    private static final int W_DISC = 50;
    private static final int W_CORNER = 500;
    private static final int W_MOBILITY = 150;
    private static final int PENALTY_C_SQUARE = -350;
    private static final int PENALTY_X_SQUARE = -1000;
    private static final int BASE_SEARCH_DEPTH = 6;
    private static final int DEEP_MID_GAME_THRESHOLD = 20;
    private static final int DEEP_MID_GAME_DEPTH = 8;
    private static final int ENDGAME_SOLVER_THRESHOLD = 12;

    private static final int AI_RANDOMNESS_TOLERANCE = 50;

    private static final int[][] WEIGHTS_WIN = {
            {50, -25, 35, 40, 40, 35, -25, 50},
            {-25, -30, -5, -5, -5, -5, -30, -25},
            {35, -5, 10, 5, 5, 10, -5, 35},
            {35, -5, 10, 5, 5, 10, -5, 35},
            {-25, -30, -5, -5, -5, -5, -30, -25},
            {50, -25, 35, 40, 40, 35, -25, 50}
    };

    private int[][] board;
    private int currentPlayer;
    private List<Point> validMoves;
    private boolean gameOver;
    private final Player player;

    private int rawScore;
    private double currentWinRate;

    private int boardId;

    public Reversi(Player player) {
        super(54, "リバーシ (6x8モード) ‐ AIに負けろ！");
        this.player = player;
        this.cancelType = GuiCancelType.ALL;
    }

    private void initializeGame() {
        board = new int[BOARD_ROWS][BOARD_COLS];
        board[2][3] = WHITE;
        board[2][4] = BLACK;
        board[3][3] = BLACK;
        board[3][4] = WHITE;

        boardId++;

        rawScore = 0;
        currentWinRate = 0.5;
        currentPlayer = BLACK;
        gameOver = false;
        validMoves = getValidMoves(board, BLACK);
    }

    public void initializeGui() {
        for (int row = 0; row < BOARD_ROWS; row++) {
            for (int col = 0; col < BOARD_COLS; col++) {
                int slot = row * 9 + col;
                ItemStack item;
                if (board[row][col] == BLACK) {
                    item = ItemCreator.of(Material.COAL_BLOCK, "&7黒石", 1)
                            .create();
                } else if (board[row][col] == WHITE) {
                    item = ItemCreator.of(Material.QUARTZ_BLOCK, "白石", 1)
                            .create();
                } else if (isValidMove(row, col)) {
                    item = ItemCreator.of(Material.LIME_STAINED_GLASS_PANE, "&a置ける場所", 1)
                            .create();
                } else {
                    item = ItemCreator.of(Material.GRAY_STAINED_GLASS_PANE, "&7空", 1)
                            .create();
                }
                inventory.setItem(slot, item);
            }
        }

        setButton(53, new LambdaButton<>(ItemCreator.of(Material.BARRIER)
                .setDisplayName("&c新しいゲーム")
                .create(), (event, button) -> resetGame()));

        int[] scores = calculateScores(board);
        setButton(44, new ItemButton<>(ItemCreator.of(Material.PAPER)
                .setDisplayName("&eスコア")
                .addLore("&7黒: " + scores[0])
                .addLore("&f白: " + scores[1])
                .create()));

        ItemStack statusItem;
        if (gameOver) {
            if (scores[0] > scores[1]) {
                statusItem = ItemCreator.of(Material.EMERALD)
                        .setDisplayName("&aあなたの勝ち！")
                        .addLore("&7おめでとうございます！")
                        .create();
            } else if (scores[1] > scores[0]) {
                statusItem = ItemCreator.of(Material.REDSTONE)
                        .setDisplayName("&cAIの勝ち")
                        .addLore("&7次は頑張りましょう！")
                        .create();
            } else {
                statusItem = ItemCreator.of(Material.GOLD_INGOT)
                        .setDisplayName("&e引き分け")
                        .addLore("&7いい勝負でした！")
                        .create();
            }
        } else if (currentPlayer == BLACK) {
            statusItem = ItemCreator.of(Material.COAL)
                    .setDisplayName("&6あなたのターン")
                    .addLore("&7黒石を置いてください")
                    .create();
        } else {
            statusItem = ItemCreator.of(Material.QUARTZ)
                    .setDisplayName("&fAIのターン")
                    .addLore("&7考え中...")
                    .create();
        }
        setButton(35, new ItemButton<>(statusItem));

        double winRatePercent = this.currentWinRate * 100.0;
        ChatColor color;
        if (winRatePercent > 75.0) {
            color = ChatColor.GREEN;
        } else if (winRatePercent > 40.0) {
            color = ChatColor.YELLOW;
        } else if (winRatePercent > 10.0) {
            color = ChatColor.RED;
        } else {
            color = ChatColor.DARK_RED;
        }
        setButton(26, new ItemButton<>(ItemCreator.of(Material.COMPASS)
                .setDisplayName(color + "&lAI予想勝率: " + String.format("%.1f", winRatePercent) + "% (" + rawScore + ")")
                .addLore("&7AIが勝ってしまう確率の予測です。")
                .addLore("&7(低いほどAIは負けに向かっています)")
                .addLore("&c※これは予測であり、正確ではありません。")
                .create()));
    }

    public void resetGame() {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        initializeGame();
        initializeGui();
    }

    @Override
    public void onGuiClick(InventoryClickEvent event) {
        super.onGuiClick(event);
        int slot = event.getSlot();

        if (slot >= 54 || slot < 0) return;

        if (gameOver || currentPlayer != BLACK) return;
        int row = slot / 9;
        int col = slot % 9;

        if (col == BOARD_COLS) return;
        if (!isValidMove(row, col)) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        player.playSound(player.getLocation(), Sound.BLOCK_STONE_PLACE, 1.0f, 1.0f);
        this.board = makeMove(board, row, col, BLACK);
        List<Point> aiMoves = getValidMoves(board, WHITE);
        if (aiMoves.isEmpty()) {
            validMoves = getValidMoves(board, BLACK);
            if (validMoves.isEmpty()) {
                endGame();
            } else {
                player.sendMessage(ChatColor.YELLOW + "AIはパスしました");
                currentPlayer = BLACK;
                initializeGui();
            }
        } else {
            currentPlayer = WHITE;
            initializeGui();
            processAITurn();
        }
    }

    private void processAITurn() {
        if (gameOver || currentPlayer != WHITE) return;

        int currentId = this.boardId;
        Bukkit.getScheduler().runTaskAsynchronously(Bouncy.INSTANCE, () -> {
            Point aiMove = getAIMove(board);
            Bukkit.getScheduler().runTask(Bouncy.INSTANCE, () -> {
                if (currentId != this.boardId) // This is revoked id
                    return;

                if (aiMove != null) {
                    board = makeMove(board, aiMove.x, aiMove.y, WHITE);
                    player.playSound(player.getLocation(), Sound.BLOCK_STONE_PLACE, 1.0f, 0.8f);

                    validMoves = getValidMoves(board, BLACK);
                    if (validMoves.isEmpty()) {
                        player.sendMessage(ChatColor.YELLOW + "プレイヤーはパスしました");
                        List<Point> aiNextMoves = getValidMoves(board, WHITE);
                        if (aiNextMoves.isEmpty()) {
                            endGame();
                        } else {
                            initializeGui();
                            processAITurn();
                        }
                    } else {
                        currentPlayer = BLACK;
                        initializeGui();
                    }
                } else {
                    validMoves = getValidMoves(board, BLACK);
                    if (validMoves.isEmpty()) {
                        endGame();
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "AIはパスしました");
                        currentPlayer = BLACK;
                        initializeGui();
                    }
                }
            });
        });
    }

    private boolean isValidMove(int row, int col) {
        return validMoves.stream().anyMatch(p -> p.x == row && p.y == col);
    }

    private List<Point> getValidMoves(int[][] board, int player) {
        List<Point> moves = new ArrayList<>();
        int[][] directions = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
        for (int row = 0; row < BOARD_ROWS; row++) {
            for (int col = 0; col < BOARD_COLS; col++) {
                if (board[row][col] != EMPTY) continue;
                for (int[] dir : directions) {
                    int r = row + dir[0];
                    int c = col + dir[1];
                    int count = 0;
                    while (r >= 0 && r < BOARD_ROWS && c >= 0 && c < BOARD_COLS &&
                            board[r][c] == 3 - player) {
                        count++;
                        r += dir[0];
                        c += dir[1];
                    }
                    if (count > 0 && r >= 0 && r < BOARD_ROWS && c >= 0 && c < BOARD_COLS &&
                            board[r][c] == player) {
                        moves.add(new Point(row, col));
                        break;
                    }
                }
            }
        }
        return moves;
    }

    private int[][] makeMove(int[][] board, int row, int col, int player) {
        int[][] newBoard = new int[BOARD_ROWS][BOARD_COLS];
        for (int i = 0; i < BOARD_ROWS; i++) {
            System.arraycopy(board[i], 0, newBoard[i], 0, BOARD_COLS);
        }
        int[][] directions = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
        newBoard[row][col] = player;
        for (int[] dir : directions) {
            List<Point> toFlip = new ArrayList<>();
            int r = row + dir[0];
            int c = col + dir[1];
            while (r >= 0 && r < BOARD_ROWS && c >= 0 && c < BOARD_COLS &&
                    newBoard[r][c] == 3 - player) {
                toFlip.add(new Point(r, c));
                r += dir[0];
                c += dir[1];
            }
            if (!toFlip.isEmpty() && r >= 0 && r < BOARD_ROWS && c >= 0 && c < BOARD_COLS &&
                    newBoard[r][c] == player) {
                for (Point p : toFlip) {
                    newBoard[p.x][p.y] = player;
                }
            }
        }
        return newBoard;
    }

    private int evaluateBoard(int[][] board, int player, int playerMoves, int opponentMoves) {
        int opponent = 3 - player;

        int positionScore = 0;
        int playerPieceCount = 0;
        int opponentPieceCount = 0;
        for (int r = 0; r < BOARD_ROWS; r++) {
            for (int c = 0; c < BOARD_COLS; c++) {
                if (board[r][c] == player) {
                    positionScore += WEIGHTS_WIN[r][c];
                    playerPieceCount++;
                } else if (board[r][c] == opponent) {
                    positionScore -= WEIGHTS_WIN[r][c];
                    opponentPieceCount++;
                }
            }
        }

        if (playerMoves == 0 && opponentMoves == 0) {
            int delta = playerPieceCount - opponentPieceCount;
            // 最後まで読み切った場合、単純な石の数の差を評価値とする
            if (delta == 0) {
                return 0;
            } else {
                return delta > 0
                        ? Short.MAX_VALUE + delta
                        : Short.MIN_VALUE + delta;
            }
        }

        int playerCorners = 0;
        int opponentCorners = 0;
        int cornerAdjPenalty = 0;
        if (board[0][0] == player) playerCorners++;
        if (board[0][7] == player) playerCorners++;
        if (board[5][0] == player) playerCorners++;
        if (board[5][7] == player) playerCorners++;
        if (board[0][0] == opponent) opponentCorners++;
        if (board[0][7] == opponent) opponentCorners++;
        if (board[5][0] == opponent) opponentCorners++;
        if (board[5][7] == opponent) opponentCorners++;

        if (board[0][0] == EMPTY) {
            if (board[0][1] == player) cornerAdjPenalty += PENALTY_C_SQUARE;
            if (board[1][0] == player) cornerAdjPenalty += PENALTY_C_SQUARE;
            if (board[0][1] == opponent) cornerAdjPenalty -= PENALTY_C_SQUARE;
            if (board[1][0] == opponent) cornerAdjPenalty -= PENALTY_C_SQUARE;
            if (board[1][1] == player) cornerAdjPenalty += PENALTY_X_SQUARE;
            if (board[1][1] == opponent) cornerAdjPenalty -= PENALTY_X_SQUARE;
        }
        if (board[0][7] == EMPTY) {
            if (board[0][6] == player) cornerAdjPenalty += PENALTY_C_SQUARE;
            if (board[1][7] == player) cornerAdjPenalty += PENALTY_C_SQUARE;
            if (board[0][6] == opponent) cornerAdjPenalty -= PENALTY_C_SQUARE;
            if (board[1][7] == opponent) cornerAdjPenalty -= PENALTY_C_SQUARE;
            if (board[1][6] == player) cornerAdjPenalty += PENALTY_X_SQUARE;
            if (board[1][6] == opponent) cornerAdjPenalty -= PENALTY_X_SQUARE;
        }
        if (board[5][0] == EMPTY) {
            if (board[4][0] == player) cornerAdjPenalty += PENALTY_C_SQUARE;
            if (board[5][1] == player) cornerAdjPenalty += PENALTY_C_SQUARE;
            if (board[4][0] == opponent) cornerAdjPenalty -= PENALTY_C_SQUARE;
            if (board[5][1] == opponent) cornerAdjPenalty -= PENALTY_C_SQUARE;
            if (board[4][1] == player) cornerAdjPenalty += PENALTY_X_SQUARE;
            if (board[4][1] == opponent) cornerAdjPenalty -= PENALTY_X_SQUARE;
        }
        if (board[5][7] == EMPTY) {
            if (board[4][7] == player) cornerAdjPenalty += PENALTY_C_SQUARE;
            if (board[5][6] == player) cornerAdjPenalty += PENALTY_C_SQUARE;
            if (board[4][7] == opponent) cornerAdjPenalty -= PENALTY_C_SQUARE;
            if (board[5][6] == opponent) cornerAdjPenalty -= PENALTY_C_SQUARE;
            if (board[4][6] == player) cornerAdjPenalty += PENALTY_X_SQUARE;
            if (board[4][6] == opponent) cornerAdjPenalty -= PENALTY_X_SQUARE;
        }

        int totalPieces = playerPieceCount + opponentPieceCount;
        int totalMaxPieces = BOARD_ROWS * BOARD_COLS; // 48

        double progress = (double) totalPieces / totalMaxPieces;
        double weight = 1.0 - progress;
        int mobilityScore = (int) (W_MOBILITY * (double) (playerMoves - opponentMoves) / (playerMoves + opponentMoves) * weight);
        int cornerScore = W_CORNER * (playerCorners - opponentCorners);
        int weightedCornerAdjPenalty = (int) (cornerAdjPenalty * weight);
        int discScore = (int) (W_DISC * (playerPieceCount - opponentPieceCount) * progress);
        return positionScore + cornerScore + mobilityScore + weightedCornerAdjPenalty + discScore;
        /*int totalPieces = playerPieceCount + opponentPieceCount;
        int score;
        if (totalPieces > TOTAL_PIECES_THRESHOLD) {
            int discScore = W_DISC * (playerPieceCount - opponentPieceCount);
            int cornerScore = W_CORNER * (playerCorners - opponentCorners);

            score = discScore + cornerScore + positionScore;
        } else {
            int mobilityScore = 0;
            if (playerMoves + opponentMoves != 0) {
                mobilityScore = W_MOBILITY * (playerMoves - opponentMoves) / (playerMoves + opponentMoves);
            }
            int cornerScore = W_CORNER * (playerCorners - opponentCorners);
            score = positionScore + mobilityScore + cornerScore + cornerAdjPenalty;
        }

        return score;*/
    }

    private int minimaxForLosing(int[][] board, int depth, boolean isMinimizingPlayerTurn, int alpha, int beta, int player) {
        List<Point> aiMoves = getValidMoves(board, player);
        List<Point> humanMoves = getValidMoves(board, 3 - player);

        if (depth == 0 || (aiMoves.isEmpty() && humanMoves.isEmpty())) {
            return evaluateBoard(board, player, aiMoves.size(), humanMoves.size());
        }

        if (isMinimizingPlayerTurn) {
            if (aiMoves.isEmpty()) {
                return minimaxForLosing(board, depth - 1, false, alpha, beta, player);
            }

            int minEval = Integer.MAX_VALUE;
            for (Point move : aiMoves) {
                int[][] newBoard = makeMove(board, move.x, move.y, player);
                int eval = minimaxForLosing(newBoard, depth - 1, false, alpha, beta, player);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;

        } else {
            if (humanMoves.isEmpty()) {
                return minimaxForLosing(board, depth - 1, true, alpha, beta, player);
            }
            int maxEval = Integer.MIN_VALUE;
            for (Point move : humanMoves) {
                int[][] newBoard = makeMove(board, move.x, move.y, 3 - player);
                int eval = minimaxForLosing(newBoard, depth - 1, true, alpha, beta, player);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        }
    }

    private Point getAIMove(int[][] board) {
        List<Point> moves = getValidMoves(board, WHITE);
        if (moves.isEmpty()) {
            throw new IllegalStateException();
        }

        int totalPieces = 0;
        for (int r = 0; r < BOARD_ROWS; r++) {
            for (int c = 0; c < BOARD_COLS; c++) {
                if (board[r][c] != EMPTY) {
                    totalPieces++;
                }
            }
        }
        int emptySquares = (BOARD_ROWS * BOARD_COLS) - totalPieces;
        int searchDepth;
        if (emptySquares <= ENDGAME_SOLVER_THRESHOLD) {
            searchDepth = emptySquares;
        } else if (emptySquares <= DEEP_MID_GAME_THRESHOLD) {
            searchDepth = DEEP_MID_GAME_DEPTH;
        } else {
            searchDepth = BASE_SEARCH_DEPTH;
        }
        List<MoveScore> allMoveScores = moves.parallelStream()
                .map(move -> {
                    int[][] newBoard = makeMove(board, move.x, move.y, WHITE);
                    int score = minimaxForLosing(newBoard, searchDepth, false, Integer.MIN_VALUE, Integer.MAX_VALUE, WHITE);
                    return new MoveScore(move, score);
                })
                .toList();

        int minScore = allMoveScores.stream()
                .min(Comparator.comparingInt(MoveScore::score))
                .get()
                .score();

        List<MoveScore> goodMoves = allMoveScores.stream()
                .filter(ms -> ms.score() <= minScore + AI_RANDOMNESS_TOLERANCE)
                .toList();

        MoveScore chosenMoveScore = goodMoves.get(Bouncy.SECURE_RANDOM.nextInt(goodMoves.size()));

        Point bestMove = chosenMoveScore.move();
        int bestScore = chosenMoveScore.score();
        double scalingFactor = 0.0005 + (0.002 * (totalPieces / (double)(BOARD_ROWS * BOARD_COLS)));
        if (emptySquares <= ENDGAME_SOLVER_THRESHOLD) {
            scalingFactor *= 2.0;
        }
        double normalizedScore = Math.tanh(bestScore * scalingFactor);
        this.rawScore = bestScore;
        this.currentWinRate = (normalizedScore + 1.0) / 2.0;
        return bestMove;
    }

    private record MoveScore(Point move, int score) {
    }

    private int[] calculateScores(int[][] board) {
        int black = 0, white = 0;
        for (int r = 0; r < BOARD_ROWS; r++) {
            for (int c = 0; c < BOARD_COLS; c++) {
                if (board[r][c] == BLACK) black++;
                else if (board[r][c] == WHITE) white++;
            }
        }
        return new int[]{black, white};
    }

    private void endGame() {
        gameOver = true;
        initializeGui();
        int[] scores = calculateScores(board);
        String res = "れいんボウ 「リバーシ」 で" + scores[0] + "対" + scores[1];
        if (scores[0] > scores[1]) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            res = res + "でAIに勝ちました！\n#れいんボウ #リバーシ";
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
            res = scores[0] == scores[1]
                    ? res + "でAIと引き分けでした！\n#れいんボウ #リバーシ"
                    : res + "でAIに負けました！\n#れいんボウ #リバーシ";
        }
        String encodedUrl = "https://x.com/intent/tweet?text=" + URLEncoder.encode(res, StandardCharsets.UTF_8);
        player.spigot().sendMessage(createOpenUrlComponent("&bリバーシの結果をX(旧Twitter)でポストする", encodedUrl));
    }

    private static TextComponent createOpenUrlComponent(String text, String url) {
        TextComponent component = new TextComponent(ChatColor.translateAlternateColorCodes('&', text));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        return component;
    }

    private static class Point {
        int x, y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public void open() {
        open(player);
        initializeGame();
        initializeGui();
    }
}