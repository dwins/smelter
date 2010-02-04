<html>
    <head>
        <title>Smelter Command Center</title>
        <style type="text/css">
        label {
            display: block;
            width: 150px;
        }
        </style>
    </head>
    <body>
        <form name="command"> <#list libraries?keys as lib>
            <label for="${lib}"><a href="/${lib}">${lib}</a></label>
            <input type="radio" name="${lib}" <#if libraries[lib] == "minified">checked="checked"</#if> value="minified"/> Minified
            <input type="radio" name="${lib}" <#if libraries[lib] == "loader">checked="checked"</#if> value="loader"/> Loader <br/> </#list>
            <input type="submit" value="Update"/>
        </form>
    </body>
</html>
