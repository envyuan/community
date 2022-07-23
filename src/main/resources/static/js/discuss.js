function like(btn,entityType,entityId,entityUserId,postId){
    $.post(//上传到服务端
        CONTEXT_PATH + "/like", //路径
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId,"postId":postId}, //数据(json形式)

        function (data){ //从服务端返回的数据
            data = $.parseJSON(data);
            if (data.code == 0) {
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?'已赞':'赞');
            }else {
                alert(data.msg);
            }
        }
    );
}