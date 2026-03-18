package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

class Player{
    String name;
    public Player(String name) {
        this.name = name;
    }
}

class Chess{
    String name; //棋子名稱
    int weight; //棋子的大小階級
    int side; //陣營
    int loc; //棋子在棋盤上的位置
    boolean isOpen = false;

    public Chess(String name,int weight,int side,int loc){
        this.name = name;
        this.weight = weight;
        this.side = side;
        this.loc = loc;
    }

    public String toString(){
        if(!this.isOpen){
            return "Ｘ";
        }else{
            return this.name;
        }
    }

}

abstract class AbstractGame{
    protected Player player1;
    protected Player player2;

    public abstract void setPlayers(Player player1,Player player2);
    public abstract boolean gameOver();
    public abstract boolean move(int location);
}

class ChessGame extends AbstractGame{
    private Chess[] board = new Chess[32]; //棋盤容量
    //記錄目前被選取準備移動的棋子位置(-1:未選取棋子)
    private  int selectedLoc = -1;

    public ChessGame(){
        generateChess();
    }

    @Override
    public void setPlayers(Player p1,Player p2){
        this.player1 = p1;
        this.player2 = p2;
    }

    @Override
    public boolean gameOver(){
        return false; //
    }

    @Override
    public boolean move(int location){
        Chess target = board[location]; //拿出玩家指定位置得棋子

        //沒有任何棋子要移動
        if(selectedLoc == -1){
            //選到空位
            if(target == null){
                System.out.println("錯誤:該位置為空");
                return false;
            }
            //若棋子未翻開
            if (!target.isOpen){
                target.isOpen = true;
                System.out.println("翻開了: " + target.name);
                return true;
            }

            //若棋子已翻開
            else{
                selectedLoc = location;
                System.out.println("已選擇: " + target.name + " 請輸入目的地");
                return false;
            }
        }

        //已有選定棋子
        else {
            Chess actor = board[selectedLoc];

            if (actor.weight == 2 && target != null) {
                // 檢查是否符合隔山打牛規則
                if (!isValidCannonCapture(selectedLoc, location)) {
                    System.out.println("錯誤：炮/包必須直線跳過一顆棋子才能吃！");
                    selectedLoc = -1;
                    return false;
                }
                // 檢查目標狀態
                if (!target.isOpen) {
                    System.out.println("錯誤：不能吃暗棋！");
                    selectedLoc = -1;
                    return false;
                }
                if (actor.side == target.side) {
                    System.out.println("錯誤：不能吃我方棋子！");
                    selectedLoc = -1;
                    return false;
                }

                // 炮吃子成功！(不受階級限制)
                System.out.println(actor.name + " 飛越炮台吃了 " + target.name);
                board[location] = actor;
                board[selectedLoc] = null;
                actor.loc = location;
                selectedLoc = -1;
                return true;
            }

            //是否相鄰移動
            if (!isAdjacent(selectedLoc,location)){
                System.out.println("錯誤:只能移動到相鄰格子");
                selectedLoc = -1; //取消選取
                return false;
            }

            //移動到空格
            if (target == null){
                board[location] = actor;
                board[selectedLoc] = null;
                actor.loc = location;
                System.out.println(actor.name + " 移動成功");
            }

            //吃子
            else {
                //不能吃暗棋
                if (!target.isOpen){
                    System.out.println("錯誤:不能吃暗棋");
                    selectedLoc = -1;
                    return false;
                }

                //不能吃同色
                if(actor.side == target.side) {
                    System.out.println("錯誤:不能吃我方棋子");
                    selectedLoc = -1;
                    return false;
                }
                    //判斷大小
                    if (canCapture(actor, target)){
                        System.out.println(actor.name + " 吃了 " + target.name);
                        board[location] = actor;
                        board[selectedLoc] = null;
                        actor.loc = location;
                    }else {
                        System.out.println("錯誤：" + actor.name + " 不能吃 " + target.name);
                        selectedLoc = -1;
                        return false;
                    }
                }
                selectedLoc = -1; //動作完成 重置選定狀態
                return true;
            }

    }

    //判斷兩個位置是否上下左右相鄰
    private boolean isAdjacent(int loc1, int loc2) {
        int r1 = loc1 / 8, c1 = loc1 % 8;
        int r2 = loc2 / 8, c2 = loc2 % 8;
        // 兩者的列距加上行距必須剛好等於 1
        return (Math.abs(r1 - r2) + Math.abs(c1 - c2)) == 1;
    }

    // 檢查炮包的跳吃是否合法
    private boolean isValidCannonCapture(int from, int to) {
        int r1 = from / 8, c1 = from % 8;
        int r2 = to / 8, c2 = to % 8;

        // 必須在同一直線或橫線上
        if (r1 != r2 && c1 != c2) return false;

        int count = 0; // 計算中間的棋子數

        if (r1 == r2) { // 同一列 (橫向移動)
            int minCol = Math.min(c1, c2);
            int maxCol = Math.max(c1, c2);
            for (int c = minCol + 1; c < maxCol; c++) {
                if (board[r1 * 8 + c] != null) count++;
            }
        } else { // 同一行 (縱向移動)
            int minRow = Math.min(r1, r2);
            int maxRow = Math.max(r1, r2);
            for (int r = minRow + 1; r < maxRow; r++) {
                if (board[r * 8 + c1] != null) count++;
            }
        }

        return count == 1;
    }

    //吃子規則判定
    private boolean canCapture(Chess actor, Chess target) {
        if (actor.weight == 2) return false; //炮包
        // 兵吃將
        if (actor.weight == 1 && target.weight == 7) return true;
        // 將不能吃兵
        if (actor.weight == 7 && target.weight == 1) return false;
        // 一般情況
        return actor.weight >= target.weight;
    }

    public void generateChess(){
        String[] redNames = {"帥","仕","仕","相","相","俥","俥","傌","傌","炮","炮","兵","兵","兵","兵","兵"};
        String[] blackNames = {"將","士","士","象","象","車","車","馬","馬","包","包","卒","卒","卒","卒","卒"};
        int[] weights = {7, 6, 6, 5, 5, 4, 4, 3, 3, 2, 2, 1, 1, 1, 1, 1};

        List<Chess> tempList = new ArrayList<>();


        for(int i = 0;i < 16;i++){
            //red
            tempList.add(new Chess(redNames[i],weights[i],0,0));

            //black
            tempList.add(new Chess(blackNames[i],weights[i],1,0));
        }

        Collections.shuffle(tempList); //打亂順序

        for(int i = 0;i < 32;i++){
            Chess c = tempList.get(i);
            c.loc = i;
            board[i] = c;
        }
    }

    public void showAllChess(){
        System.out.println("   1  2  3 4  5 6  7 8");
        char[] rowLabels = {'A', 'B', 'C', 'D'};

        for (int r = 0;r < 4;r++){
            System.out.print(rowLabels[r] + " ");
            for (int c = 0;c < 8;c++){
                int index = r * 8 + c;

                Chess piece = board[index];
                if (piece == null){
                    System.out.print(" _");
                }else{
                    System.out.print(" " + piece.toString());
                }

            }
            System.out.println(); // 換行
        }

    }

}

public class Main {
    public static void main(String[] args) {
        ChessGame game = new ChessGame();

        game.setPlayers(new Player("Player 1"),new Player("Player 2"));

        //接收輸入
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome!");

        //遊戲若尚未結束 就一直迴圈
        while (!game.gameOver()){
            game.showAllChess();
            System.out.print("輸入操作位置(如 A1,B3): ");
            //讀取輸入並轉成大寫、去除空白
            String input = scanner.nextLine().toUpperCase().trim();

            //格式檢查
            if (input.length() != 2){
                System.out.println("輸入格式錯誤 請重新輸入");
                continue;
            }

            char rowChar = input.charAt(0);
            char colChar = input.charAt(1);

            //檢查輸入
            if (rowChar < 'A' || rowChar > 'D' || colChar < '1' || colChar > '8'){
                System.out.println("超出範圍 重新輸入");
                continue;
            }
            int row = rowChar - 'A';
            int col = colChar - '1';
            int location = row * 8 + col;

            game.move(location);

        }
        scanner.close();
    }
}