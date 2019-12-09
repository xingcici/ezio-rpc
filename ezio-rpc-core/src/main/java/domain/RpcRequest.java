package domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @creed: Here be dragons !
 * @author: Ezio
 * @Time: 2019/10/23 7:41 下午
 * @desc:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest {

    private String id;

    private String className;

    private String methodName;

    private String[] parameterTypes;

    private Object[] parameters;


}
