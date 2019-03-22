package com.tale.service;

import com.blade.exception.ValidatorException;
import com.blade.ioc.annotation.Bean;
import com.blade.ioc.annotation.Inject;
import com.blade.kit.BladeKit;
import com.blade.kit.DateKit;
import com.blade.kit.EncryptKit;
import com.blade.kit.StringKit;
import com.tale.bootstrap.TaleConst;
import com.tale.model.dto.*;
import com.tale.model.entity.*;
import com.tale.utils.MapCache;
import com.tale.utils.TaleUtils;
import io.github.biezhi.anima.enums.OrderBy;
import io.github.biezhi.anima.page.Page;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.tale.bootstrap.TaleConst.CLASSPATH;
import static com.tale.bootstrap.TaleConst.COMMENT_APPROVED;
import static io.github.biezhi.anima.Anima.select;

/**
 * 站点Service
 *
 * @author biezhi
 * @since 1.3.1
 */
@Bean
public class SiteService {

    @Inject
    private CommentsService commentsService;

    public MapCache mapCache = new MapCache();

    /**
     * 初始化站点
     *
     * @param users 用户
     */
    public void initSite(Users users) {
        String pwd = EncryptKit.md5(users.getUsername() + users.getPassword());
        users.setPassword(pwd);
        users.setScreenName(users.getUsername());
        users.setCreated(DateKit.nowUnix());
        Integer uid = users.save().asInt();

        try {
            String cp   = SiteService.class.getClassLoader().getResource("").getPath();
            File   lock = new File(cp + "install.lock");
            lock.createNewFile();
            TaleConst.INSTALLED = Boolean.TRUE;
            new Logs("初始化站点", null, "", uid).save();
        } catch (Exception e) {
            throw new ValidatorException("初始化站点失败");
        }
    }

    /**
     * 最新收到的评论
     *
     * @param limit 评论数
     */
    public List<Comments> recentComments(int limit) {
        if (limit < 0 || limit > 10) {
            limit = 10;
        }

        Page<Comments> commentsPage = select().from(Comments.class)
                                              .where(Comments::getStatus, COMMENT_APPROVED)
                                              .order(Comments::getCreated, OrderBy.DESC)
                                              .page(1, limit);
        return commentsPage.getRows();
    }

    /**
     * 根据类型获取文章列表
     *
     * @param type 最新,随机
     * @param limit 获取条数
     */
    public List<Contents> getContens(String type, int limit) {

        if (limit < 0 || limit > 20) {
            limit = 10;
        }

        // 最新文章
        if (Types.RECENT_ARTICLE.equals(type)) {
            Page<Contents> contentsPage = select().from(Contents.class)
                                                  .where(Contents::getStatus, Types.PUBLISH)
                                                  .and(Contents::getType, Types.ARTICLE)
                                                  .order(Contents::getCreated, OrderBy.DESC)
                                                  .page(1, limit);

            return contentsPage.getRows();
        }

        // 随机文章
        if (Types.RANDOM_ARTICLE.equals(type)) {
            List<Integer> cids = select().bySQL(Integer.class,
                                                "select cid from t_contents where type = ? and status = ? order by random() * cid limit ?",
                                                Types.ARTICLE, Types.PUBLISH, limit).all();
            if (BladeKit.isNotEmpty(cids)) {
                return select().from(Contents.class).in(Contents::getCid, cids).all();
            }
        }
        return new ArrayList<>();
    }

    /**
     * 获取后台统计数据
     */
    public Statistics getStatistics() {

        Statistics statistics = mapCache.get(Types.SYS_STATISTICS);
        if (null != statistics) {
            return statistics;
        }

        statistics = new Statistics();

        long articles = select().from(Contents.class).where(Contents::getType, Types.ARTICLE)
                                .and(Contents::getStatus, Types.PUBLISH).count();
        long pages = select().from(Contents.class).where(Contents::getType, Types.PAGE)
                             .and(Contents::getStatus, Types.PUBLISH).count();
        long comments   = select().from(Comments.class).count();
        long attachs    = select().from(Attach.class).count();
        long tags       = select().from(Metas.class).where(Metas::getType, Types.TAG).count();
        long categories = select().from(Metas.class).where(Metas::getType, Types.CATEGORY).count();

        statistics.setArticles(articles);
        statistics.setPages(pages);
        statistics.setComments(comments);
        statistics.setAttachs(attachs);
        statistics.setTags(tags);
        statistics.setCategories(categories);

        mapCache.set(Types.SYS_STATISTICS, statistics);
        return statistics;
    }

    /**
     * 查询文章归档
     */
    public List<Archive> getArchives() {
        String sql =
            "select strftime('%Y年%m月', datetime(created, 'unixepoch') ) as date_str, count(*) as count  from t_contents "
                +
                "where type = 'post' and status = 'publish' group by date_str order by date_str desc";

        List<Archive> archives = select().bySQL(Archive.class, sql).all();
        if (null != archives) {
            return archives.stream()
                           .map(this::parseArchive)
                           .collect(Collectors.toList());
        }
        return new ArrayList<>(0);
    }

    private Archive parseArchive(Archive archive) {
        String dateStr = archive.getDateStr();
        Date   sd      = DateKit.toDate(dateStr + "01", "yyyy年MM月dd");
        archive.setDate(sd);
        int      start    = DateKit.toUnix(sd);
        Calendar calender = Calendar.getInstance();
        calender.setTime(sd);
        calender.add(Calendar.MONTH, 1);
        Date endSd = calender.getTime();
        int  end   = DateKit.toUnix(endSd) - 1;

        List<Contents> contents = select().from(Contents.class)
                                          .where(Contents::getType, Types.ARTICLE)
                                          .and(Contents::getStatus, Types.PUBLISH)
                                          .and(Contents::getCreated).gt(start)
                                          .and(Contents::getCreated).lt(end)
                                          .order(Contents::getCreated, OrderBy.DESC)
                                          .all();

        archive.setArticles(contents);
        return archive;
    }

    /**
     * 查询一条评论
     *
     * @param coid 评论主键
     */
    public Comments getComment(Integer coid) {
        if (null != coid) {
            return select().from(Comments.class).byId(coid);
        }
        return null;
    }

    /**
     * 获取分类/标签列表
     */
    public List<Metas> getMetas(String searchType, String type, int limit) {

        if (StringKit.isBlank(searchType) || StringKit.isBlank(type)) {
            return new ArrayList<>(0);
        }

        if (limit < 1 || limit > TaleConst.MAX_POSTS) {
            limit = 10;
        }

        // 获取最新的项目
        if (Types.RECENT_META.equals(searchType)) {
            String sql =
                "select a.*, count(b.cid) as count from t_metas a left join `t_relationships` b on a.mid = b.mid "
                    +
                    "where a.type = ? group by a.mid order by count desc, a.mid desc limit ?";

            return select().bySQL(Metas.class, sql, type, limit).all();
        }

        // 随机获取项目
        if (Types.RANDOM_META.equals(searchType)) {
            List<Integer> mids = select().bySQL(Integer.class,
                                                "select mid from t_metas where type = ? order by random() * mid limit ?",
                                                type, limit).all();
            if (BladeKit.isNotEmpty(mids)) {
                String in = TaleUtils.listToInSql(mids);
                String sql =
                    "select a.*, count(b.cid) as count from t_metas a left join `t_relationships` b on a.mid = b.mid "
                        +
                        "where a.mid in " + in + "group by a.mid order by count desc, a.mid desc";

                return select().bySQL(Metas.class, sql).all();
            }
        }
        return new ArrayList<>(0);
    }

    /**
     * 获取相邻的文章
     *
     * @param type 上一篇:prev | 下一篇:next
     * @param created 当前文章创建时间
     */
    public Contents getNhContent(String type, Integer created) {
        Contents contents = null;
        if (Types.NEXT.equals(type)) {
            contents = select().bySQL(Contents.class,
                                      "SELECT * FROM t_contents WHERE type = ? AND status = ? AND created > ? ORDER BY created ASC LIMIT 1",
                                      Types.ARTICLE, Types.PUBLISH, created).one();
        }
        if (Types.PREV.equals(type)) {
            contents = select().bySQL(Contents.class,
                                      "SELECT * FROM t_contents WHERE type = ? AND status = ? AND created < ? ORDER BY created DESC LIMIT 1",
                                      Types.ARTICLE, Types.PUBLISH, created).one();
        }
        return contents;
    }

    /**
     * 获取文章的评论
     *
     * @param cid 文章id
     * @param page 页码
     * @param limit 每页条数
     */
    public Page<Comment> getComments(Integer cid, int page, int limit) {
        return commentsService.getComments(cid, page, limit);
    }

    /**
     * 获取文章的评论总数
     *
     * @param cid 文章id
     */
    public long getCommentCount(Integer cid) {
        return commentsService.getCommentCount(cid);
    }

    /**
     * 清楚缓存
     *
     * @param key 缓存key
     */
    public void cleanCache(String key) {
        if (StringKit.isNotBlank(key)) {
            if ("*".equals(key)) {
                mapCache.clean();
            } else {
                mapCache.del(key);
            }
        }
    }

}
