package cart.dao;

import cart.entity.CartAddedProduct;
import cart.entity.Product;
import cart.entity.vo.Email;
import cart.exception.TableIdNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JdbcCartAddedProductDao implements cartAddedProductDao {

    private static final String CART_TABLE = "cart_added_product";
    private static final String PRODUCT_TABLE = "products";

    private static final RowMapper<CartAddedProduct> cartAddedProductRowMapper = (rs, rowNum) ->
            new CartAddedProduct(
                    rs.getLong(CART_TABLE + ".id"),
                    new Email(rs.getString(CART_TABLE + ".user_email")),
                    new Product(
                            rs.getLong(PRODUCT_TABLE + ".id"),
                            rs.getString(PRODUCT_TABLE + ".product_name"),
                            rs.getInt(PRODUCT_TABLE + ".product_price"),
                            rs.getString(PRODUCT_TABLE + ".product_image")
                    )
            );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcCartAddedProductDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("cart_added_product")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public long insert(final Email email, final Product product) {
        final MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("user_email", email.getValue())
                .addValue("product_id", product.getId());

        return simpleJdbcInsert.executeAndReturnKey(parameterSource).longValue();
    }

    @Override
    public CartAddedProduct findById(final long id) {
        final String sql = "SELECT * FROM cart_added_product " +
                "JOIN products " +
                "ON cart_added_product.product_id = products.id " +
                "WHERE cart_added_product.id = ?;";

        try {
            return jdbcTemplate.queryForObject(sql, cartAddedProductRowMapper, id);
        } catch (EmptyResultDataAccessException exception) {
            throw new TableIdNotFoundException("해당 카트 id를 찾을 수 없습니다. 입력된 카트 id : " + id);
        }
    }

    @Override
    public List<CartAddedProduct> findProductsByUserEmail(final Email userEmail) {
        final String sql = "SELECT * FROM cart_added_product " +
                "JOIN products " +
                "ON cart_added_product.product_id = products.id  " +
                "WHERE user_email = ?;";
        return jdbcTemplate.query(sql, cartAddedProductRowMapper, userEmail.getValue());
    }

    @Override
    public void delete(final long id) {
        final String sql = "DELETE FROM cart_added_product WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
