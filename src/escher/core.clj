(ns escher.core
  (require [quil.core :as q])
  (:gen-class))

(def width 600)
(def height 600)

(def draw-line q/line)

(def whole-window {:origin [0 0]
                   :e1 [width 0]
                   :e2 [0 height]})

(def frame1 {:origin [200 50]
             :e1 [200 100]
             :e2 [150 200]})

(def frame2 {:origin [50 50]
             :e1 [100 0]
             :e2 [0 200]})

(defn add-vec [[x1 y1] [x2 y2]]
  ;; COMPLETE (Ex 2.46)
    [(+ x1 x2)  (+ y1 y2)]
 )

(defn sub-vec [[x1 y1] [x2 y2]]
  ;; COMPLETE
    [(- x1 x2)  (- y1 y2)]
  )

(defn scale-vec [[x y] s]
  ;; COMPLETE
    [(* x s)  (* y s)]

  )

;; 返回一个坐标映射函数，其中 0 <= x, y <= 1
;; 结果是frame中的一个点
(defn frame-coord-map
  [{:keys [origin e1 e2]}]
  (fn [[x y]]
    (add-vec origin
             (add-vec (scale-vec e1 x)
                      (scale-vec e2 y)))))

;;绘制frame
(defn frame-painter [{:keys [origin e1 e2]}]
  (let [corner (add-vec origin (add-vec e1 e2))]
    (draw-line origin (add-vec origin e1))
    (draw-line origin (add-vec origin e2))
    (draw-line (add-vec origin e2) corner)
    (draw-line (add-vec origin e1) corner)))

;;返回一个函数，在frame中顺序绘制segment-list中的线
(defn segment-painter [segment-list]
  (fn [frame]
    (let [m (frame-coord-map frame)]
      (doseq [[start end] segment-list]
        (draw-line (m start) (m end))))))

;;
(defn transform-picture [p origin e1 e2]
  (fn [frame]
    (let [map (frame-coord-map frame)
          new-origin (map origin)]
      (p {:origin new-origin
          :e1 (sub-vec (map e1) new-origin)
          :e2 (sub-vec (map e2) new-origin)}))))

;;水平翻转
(defn flip-vert [p]
  (transform-picture p [0 1] [1 1] [0 0]))

;;竖直翻转
(defn flip-horiz [p]
  ;; COMPLETE (Ex 2.50)
  (transform-picture p [1 0] [0 0] [1 1])
  )

;;顺时针旋转90度
(defn rotate [p]
  ;; COMPLETE
  (transform-picture p [0 1] [0 0] [1 1])
  )

(defn rotate180 [p]
  (rotate (rotate p)))

(defn rotate270 [p]
  (rotate (rotate (rotate p))))

;;绘制左右两个相同的frame
(defn beside [p1 p2]
  (let [split [0.5 0]
        left (transform-picture p1 [0 0] split [0 1])
        right (transform-picture p2 split [1 0] [0.5 1])]
    (fn [frame]
      (left frame)
      (right frame))))

;;绘制上下两个相同的frame
(defn above [p1 p2]
  ; COMPLETE (Ex 2.51)
    (let [split [0 0.5]
         top (transform-picture p1 split [0 1] [1 0.5])
         bottom (transform-picture p2 [0 0] split [1 0])]
     (fn [frame]
       (top frame)
       (bottom frame)))
  )

(defn path [& veclist]
  ; COMPLETE
  (fn [frame]
    (let [m (frame-coord-map frame)]
        (doseq [[start end] veclist]
        (draw-line (m start) (m end)))))

  )

;;绘制四等分图片
(defn quartet [p1 p2 p3 p4]
  (above (beside p1 p2)
         (beside p3 p4)))

(defn square-of-four [tl tr
                      bl br]
  (fn [p]
    (let [top (beside (tl p) (tr p))
          bottom (beside (bl p) (br p))]
      (above top
             bottom))))

(defn right-split [p n]
  (if (= n 0)
    p
    (let [smaller (right-split p (dec n))]
      (beside p (above smaller smaller)))))

;;上下划分
(defn up-split [p n]
  ;; COMPLETE (Ex 2.44)
  (if (= n 0)
    p 
    (let [smaller (up-split p (dec n))]
      (above p (beside smaller smaller))))
  )


(defn split [f g]
  ; COMPLETE (Ex 2.45)
  "Should be able to do
    (def right-split (split beside above))
    (def up-split (split above beside)
  and replace the existing *-split fns"
  (fn split-helper [p n]
    (if (= n 0)
      p
      (let [smaller (split-helper p (dec n))]
        (f p (g smaller smaller))))
    ) 
  )

(defn corner-split [p n]
  (if (= n 0)
    p
    (let [up (up-split p (dec n))
          right (right-split p (dec n))
          top-left (beside up up)
          bottom-right (above right right)
          top-right (corner-split p (dec n))]
      (beside (above top-left p)
              (above top-right bottom-right)))))

(def combine-four (square-of-four flip-horiz
                                  identity
                                  rotate180
                                  flip-vert))

(defn square-limit [p n]
  (combine-four (corner-split p n)))

; Ex2.49, Make these shapes with segment-painter/path
(def box (segment-painter [[[0 0] [0 1]]
                           [[0 1] [1 1]]
                           [[1 1] [1 0]]
                           [[1 0] [0 0]]]))
(def x (segment-painter [[[0 0] [1 1]] 
                         [[0 1] [1 0]]]))

(def diamond (segment-painter [[[0.0 0.5] [0.5 1.0]] 
                               [[0.5 1.0] [1.0 0.5]]
                               [[1.0 0.5] [0.5 0.0]]
                               [[0.5 0.0] [0.0 0.5]]]))

(def george (segment-painter [
                              [[0.25 0] [0.35 0.5]]
                              [[0.35 0.5] [0.3 0.6]]
                              [[0.3 0.6] [0.15 0.4]]
                              [[0.15 0.4] [0 0.65]]
                              [[0 0.65] [0 0.85]]
                              [[0 0.85] [0.15 0.6]]
                              [[0.15 0.6] [0.3 0.65]]
                              [[0.3 0.65] [0.4 0.65]]
                              [[0.4 0.65] [0.35 0.85]]
                              [[0.35 0.85] [0.4 1]]
                              [[0.4 1] [0.6 1]]
                              [[0.6 1] [0.65 0.85]]
                              [[0.65 0.85] [0.6 0.65]]
                              [[0.6 0.65] [0.75 0.65]]
                              [[0.75 0.65] [1 0.35]]
                              [[1 0.35] [1 0.15]]
                              [[1 0.15] [0.6 0.45]]
                              [[0.6 0.45] [0.75 0]]
                              [[0.75 0] [0.6 0]]
                              [[0.6 0] [0.5 0.3]]
                              [[0.5 0.3] [0.4 0]]
                              [[0.4 0] [0.25 0]]
                              ]))

(defn draw [picture]
  (picture whole-window))



(defn image-painter [img]
  (fn [{[ox oy] :origin
        [e1x e1y] :e1
        [e2x e2y] :e2
        }]
    (let [width (.width img)
          height (.height img)]
      ; COMPLETE  
      (q/image img ox oy)
      )))

(def diag (segment-painter [[[0 0] [1 1]]]))

(defn draw-image []
  (let [man (image-painter (q/load-image "data/man.gif"))
        bruce (image-painter (q/load-image "data/bruce.jpg"))
        angels (image-painter (q/load-image "data/angels.jpg"))]
    (q/background 255)
    ;; (frame-painter frame1) 
    ;; (frame-painter frame2)
    ;; (draw x)
    ;; (draw diamond) 
    ;; (george frame2)
    ;;  (draw (rotate george))
    ;;  (draw (flip-horiz george))
    ;;(draw (beside box box))
    ;;(draw (combine-four george))
    ;; (draw (beside (above george george)
    ;;                (flip-horiz (above george george))))
    ;; (draw (above (beside george (flip-horiz george))
    ;;              (beside george (flip-horiz george))))

    (draw ((square-of-four identity flip-vert
                           flip-horiz rotate)
           george))

    ; Needs image-painter
    ;;(bruce frame1)
    ;;(bruce frame2)
    ;; (draw (beside george bruce))
    ;; (draw (corner-split bruce 4))
    ;; (draw (square-limit bruce 3))
    ;; (draw (beside  bruce (above  bruce
    ;;                              george)))
    ))

(q/defsketch escher
  :title "Escher"
  :draw draw-image
  :size [width height])

(defn -main [])
