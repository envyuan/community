package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);
    //替换符
    private static final String REPLACEMENT = "***";

    //step1. 定义前缀树结构（使用内部类）
    private class TrieNode {
        //是否是关键词结束标志（树叶节点）
        public boolean keyWordEnd = false;

        //子节点（key是下级字符，value是下级节点）
        private Map<Character,TrieNode> subNodes = new HashMap<>();

        public boolean isKeyWordEnd(){
            return keyWordEnd;
        }
        public void setKeyWordEnd(boolean kwe){
            keyWordEnd = kwe;
        }

        //添加子节点
        public void addSubNode(Character c, TrieNode node){
            subNodes.put(c,node);
        }
        //获取子节点
        public TrieNode getSubNode(Character c){
            TrieNode trieNode = subNodes.get(c);
            return trieNode;
        }
    }

    //step2. 初始化前缀树
    //根节点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct //构造器初始化之后自动执行
    public void init(){
        try (
            //在编译好的class文件中获取文件（？？）
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword=reader.readLine()) != null){
                //添加到前缀树
                this.addKeyWord(keyword);
            }
        } catch (Exception e) {
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }
    }

    //step3. 过滤敏感词
    public String filter(String text){
        if (text == null){
            return null;
        }

        TrieNode tempnode = rootNode;//指针1
        int start = 0;//指针2
        int end = 0;//指针3
        StringBuilder filtedText = new StringBuilder();

        while (start < text.length()){
            Character c = text.charAt(end);
            //跳过特殊符号
            boolean symbol = !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2e80 || c > 0x9fff);//0x2e80-0x9fff为东亚文字范围
            if (symbol){
                //若指针1处于根节点，将此符号计入结果，指针2向下走一步
                if (tempnode == rootNode){
                    filtedText.append(c);
                    start++;
                }
                //无论符号在开头还是中间，指针3都向下走一步
                end++;
                continue;
            }

            tempnode = tempnode.getSubNode(c);//下级节点
            //非过滤字符
            if (tempnode == null){
                filtedText.append(text.charAt(start));
                start++;
                end = start;
                tempnode = rootNode;
            }else if(tempnode.isKeyWordEnd()){ //是结束字符
                //替换
                filtedText.append(REPLACEMENT);
                end++;
                start = end;
                tempnode = rootNode;
            }else {
                //检查下一个字符
                end++;
            }
        }

        //将最后一批字符计入结果
        //filtedText.append(text.substring(start));
        return filtedText.toString();
    }


    //将一个敏感词添加到前缀树中
    private void addKeyWord(String keyword){
        TrieNode tempNode = rootNode;//指向前缀树的指针
        for (int i = 0; i < keyword.length(); i++) {
            Character c = keyword.charAt(i);

            TrieNode subnode = tempNode.getSubNode(c);

            //没有子节点或子节点不包含此字符
            if (subnode == null){
                subnode = new TrieNode();
                tempNode.addSubNode(c,subnode);
            }
            //指针指向子节点，进行下一循环
            tempNode = subnode;
            //标记最后一个节点
            if (i == keyword.length()-1){
                tempNode.setKeyWordEnd(true);
            }
        }
    }

}
