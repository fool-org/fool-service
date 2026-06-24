package org.fool.framework.query;

public class PagedQuerySql {
    private final String countSql;
    private final String pageSql;
    private final Object[] countArgs;
    private final Object[] pageArgs;

    public PagedQuerySql(String countSql, String pageSql, Object[] countArgs, Object[] pageArgs) {
        this.countSql = countSql;
        this.pageSql = pageSql;
        this.countArgs = countArgs;
        this.pageArgs = pageArgs;
    }

    public String getCountSql() {
        return countSql;
    }

    public String getPageSql() {
        return pageSql;
    }

    public Object[] getCountArgs() {
        return countArgs;
    }

    public Object[] getPageArgs() {
        return pageArgs;
    }

    public String combinedSql() {
        return countSql + "\n"
                + "            " + pageSql + "\n"
                + "            ";
    }

    public Object[] combinedArgs() {
        Object[] args = new Object[countArgs.length + pageArgs.length];
        System.arraycopy(countArgs, 0, args, 0, countArgs.length);
        System.arraycopy(pageArgs, 0, args, countArgs.length, pageArgs.length);
        return args;
    }
}
