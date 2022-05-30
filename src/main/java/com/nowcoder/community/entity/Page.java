package com.nowcoder.community.entity;

public class Page {
    private int current = 1;//当前第几页
    private int rows;//数据总数
    private int limit = 10;//每页条数
    private String path;//访问路径

    public int getCurrent(){
        return current;
    }
    public void setCurrent(int current){
        if (current >= 1){
            this.current=current;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows>=0){
            this.rows = rows;
        }

    }

    public int getLimit(){
        return limit;
    }
    public void setLimit(int limit){
        if (limit >= 1 && limit <=100){
            this.limit=limit;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getTotal(){
        if (rows % limit == 0) {
            return rows/limit;
        }else{
            return rows/limit+1;
        }
    }

    public int getOffset(){
        return (current-1)*limit;
    }

    public int getFrom(){
        if (current-2 >=1){
            return current-2;
        }else{
            return 1;
        }

    }
    public int getTo(){
        int total = getTotal();
        if (current+2 <= total){
            return current+2;
        }else{
            return total;
        }
    }

}
