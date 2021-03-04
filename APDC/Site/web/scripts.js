/**
 * Created by Goncalo on 06/03/2017.
 */

function load(pageToLoad) {
    var content = $("#content");
    var footer = $("#footer");
    content.fadeOut('slow', function () {
        footer.css("position", "absolute");
        content.load(pageToLoad, function () {
            footer.css("position", "static");
            content.fadeIn('slow', function () {
            });
        });
    });
}

function scroll(divToScrollTo) {
    $('html, body').animate({scrollTop:$(divToScrollTo).position().top}, 'medium');
}


function moveScroller() {
    var $anchor = $("#anchor");
    var $scroller = $('#points');

    var move = function() {
        var st = $(window).scrollTop();
        var ot = $anchor.offset().top;
        if(st > ot) {
            $scroller.css({
                position: "fixed",
                top: "0px"
            });
        } else {
            if(st <= ot) {
                $scroller.css({
                    position: "relative",
                    top: ""
                });
            }
        }
    };
    $(window).scroll(move);
    move();
}