import java.security.Permission;

public class MySecurityManager extends SecurityManager{
    @Override
    public void checkPermission(Permission perm) {
        System.out.println("默认不做任何限制");
        //throw new RuntimeException("checkPermission权限异常："+ perm);
    }

    @Override
    public void checkExec(String cmd) {
        super.checkExec(cmd);
        throw new RuntimeException("checkExec权限异常："+ cmd);

    }

    @Override
    public void checkRead(String file, Object context) {
        super.checkRead(file, context);
        //啥也读不了，需要给一些东西放行，可能需要使用白名单，还是和黑名单一样麻烦，之后用Docker来解决
        throw new RuntimeException("checkRead权限异常："+ context);

    }

    @Override
    public void checkWrite(String file) {
        super.checkWrite(file);
        throw new RuntimeException("checkWrite权限异常："+ file);

    }

    @Override
    public void checkConnect(String host, int port) {
        super.checkConnect(host, port);
        throw new RuntimeException("checkConnect权限异常："+ port);
    }

    @Override
    public void checkDelete(String file) {
        super.checkDelete(file);
        throw new RuntimeException("checkDelete权限异常："+ file);
    }
}
